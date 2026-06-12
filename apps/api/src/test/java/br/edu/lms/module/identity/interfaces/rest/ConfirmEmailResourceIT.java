package br.edu.lms.module.identity.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class ConfirmEmailResourceIT {

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
