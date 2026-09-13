package br.edu.lms.module.identity.interfaces.rest;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

@QuarkusTest
class ConfirmEmailResourceIT {

    static final String MAIL_EMAIL = "mail-confirm-it@test.com";

    @Inject MockMailbox mailbox;
    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void setUp() {
        mailbox.clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM users WHERE email = ?").setParameter(1, MAIL_EMAIL).executeUpdate();
        tx.commit();
    }

    @Test
    void confirmEmail_missingToken_returns400() {
        given()
                .when().get("/auth/confirm-email")
                .then()
                .statusCode(400);
    }

    @Test
    void confirmEmail_invalidToken_returns400() {
        given()
                .queryParam("token", "non-existent-token-uuid")
                .when().get("/auth/confirm-email")
                .then()
                .statusCode(400)
                .body("error", equalTo("INVALID_CONFIRMATION_TOKEN"));
    }

    // O e-mail de confirmação sai do layout orgânico compartilhado
    // (templates/mail), com o link vindo de lms.app.base-url (#73).
    @Test
    void register_sendsTheConfirmationEmailFromTheOrganicTemplate() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"fullName":"Mail Confirm IT","email":"%s","password":"Senha@12345"}
                        """.formatted(MAIL_EMAIL))
                .when().post("/auth/register")
                .then()
                .statusCode(lessThan(300));

        var mails = mailbox.getMailsSentTo(MAIL_EMAIL);
        assertThat(mails).hasSize(1);
        assertThat(mails.get(0).getSubject()).isEqualTo("Confirme seu e-mail — LMS Nexus");
        assertThat(mails.get(0).getHtml())
                .containsPattern("href=\"http://localhost:5173/confirm-email\\?token=[0-9a-f-]{36}\"")
                .contains("Confirmar e-mail")
                .contains("O link expira em 24 horas")
                .contains("Nexus")
                .contains("background-color:#c67139");
    }

    @Test
    void resendConfirmation_unknownEmail_returns204() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"unknown@test.com"}
                        """)
                .when().post("/auth/resend-confirmation")
                .then()
                .statusCode(204);
    }

    @Test
    void resendConfirmation_invalidEmail_returns422() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"not-an-email"}
                        """)
                .when().post("/auth/resend-confirmation")
                .then()
                .statusCode(422);
    }

    @Test
    void resendConfirmation_missingEmail_returns422() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/auth/resend-confirmation")
                .then()
                .statusCode(422);
    }
}
