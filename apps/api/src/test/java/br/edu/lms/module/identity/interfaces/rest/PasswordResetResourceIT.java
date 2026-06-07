package br.edu.lms.module.identity.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class PasswordResetResourceIT {

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

    @Test
    void resetPassword_invalidToken_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"token":"invalid-token","newPassword":"newpassword123"}
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
