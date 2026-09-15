package br.edu.lms.module.identity.interfaces.rest;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

// Senha fraca é recusada pela própria API, não só pelo formulário do front (#229).
@QuarkusTest
class StrongPasswordResourceIT {

    static final String EMAIL = "weak-password-it@test.com";

    @Inject MockMailbox mailbox;

    @BeforeEach
    void setUp() {
        mailbox.clear();
    }

    @Test
    void register_passwordMissingCriteria_returns422ListingThem() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"fullName":"Weak Password IT","email":"%s","password":"newpassword123"}
                        """.formatted(EMAIL))
                .when().post("/auth/register")
                .then()
                .statusCode(422)
                .body("errors", hasItem(containsString("A senha precisa de: uma maiúscula, um símbolo")));

        assertThat(mailbox.getMailsSentTo(EMAIL)).isEmpty();
    }

    @Test
    void register_passwordShorterThanMinimum_returns422() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"fullName":"Weak Password IT","email":"%s","password":"Aa1!"}
                        """.formatted(EMAIL))
                .when().post("/auth/register")
                .then()
                .statusCode(422)
                .body("errors", hasItem(containsString("Senha deve ter no mínimo 8 caracteres")));

        assertThat(mailbox.getMailsSentTo(EMAIL)).isEmpty();
    }

    @Test
    void resetPassword_passwordMissingCriteria_returns422ListingThem() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"token":"any-token","newPassword":"SENHASEGURA"}
                        """)
                .when().post("/auth/reset-password")
                .then()
                .statusCode(422)
                .body("errors", hasItem(containsString("A senha precisa de: uma minúscula, um número, um símbolo")));
    }
}
