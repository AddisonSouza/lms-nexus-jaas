package br.edu.lms.module.identity.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthResourceIT {

    @Test
    void login_invalidCredentials_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email":"nonexistent@test.com","password":"wrong"}
                        """)
                .when().post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void refresh_withoutCookie_returns401() {
        given()
                .when().post("/auth/refresh")
                .then()
                .statusCode(401);
    }

    @Test
    void logout_withoutToken_returns401() {
        given()
                .when().post("/auth/logout")
                .then()
                .statusCode(401);
    }
}
