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

@QuarkusTest
class PasswordResetResourceIT {

    static final String ACTIVE_USER_ID = "55555555-5555-5555-5555-555555555555";
    static final String ACTIVE_EMAIL = "mail-reset-it@test.com";

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
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, ACTIVE_USER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    void forgotPassword_anyEmail_returns204() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"nonexistent@test.com"}
                        """)
                .when().post("/auth/forgot-password")
                .then()
                .statusCode(204);
    }

    @Test
    void forgotPassword_existingEmail_returns204() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"user@existing.com"}
                        """)
                .when().post("/auth/forgot-password")
                .then()
                .statusCode(204);
    }

    // O e-mail de redefinição sai do layout orgânico compartilhado
    // (templates/mail), com o mesmo assunto, link e prazo de antes (#73).
    @Test
    void forgotPassword_activeUser_sendsTheResetEmailFromTheOrganicTemplate() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, ACTIVE_USER_ID)
                .setParameter(2, "Mail Reset IT")
                .setParameter(3, ACTIVE_EMAIL)
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
        tx.commit();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"%s"}
                        """.formatted(ACTIVE_EMAIL))
                .when().post("/auth/forgot-password")
                .then()
                .statusCode(204);

        var mails = mailbox.getMailsSentTo(ACTIVE_EMAIL);
        assertThat(mails).hasSize(1);
        assertThat(mails.get(0).getSubject()).isEqualTo("Redefinição de senha — LMS Nexus");
        assertThat(mails.get(0).getHtml())
                .containsPattern("href=\"http://localhost:5173/reset-password\\?token=[0-9a-f-]{36}\"")
                .contains("Redefinir senha")
                .contains("O link expira em 1 hora")
                .contains("Nexus")
                .contains("background-color:#c67139");
    }

    @Test
    void resetPassword_invalidToken_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"token":"invalid-token","newPassword":"NewPassword@123"}
                        """)
                .when().post("/auth/reset-password")
                .then()
                .statusCode(400);
    }

    @Test
    void resetPassword_missingFields_returns422() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/auth/reset-password")
                .then()
                .statusCode(422);
    }
}
