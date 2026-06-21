package br.edu.lms.module.curriculum.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ContentResourceIT {

    @Test
    void listContents_withoutToken_returns401() {
        given()
                .when().get("/subjects/any-id/contents")
                .then()
                .statusCode(401);
    }

    @Test
    void createContent_withoutToken_returns401() {
        given()
                .contentType("multipart/form-data")
                .when().post("/subjects/any-id/contents")
                .then()
                .statusCode(401);
    }

    @Test
    void updateContent_withoutToken_returns401() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().put("/subjects/any-id/contents/content-id")
                .then()
                .statusCode(401);
    }

    @Test
    void deleteContent_withoutToken_returns401() {
        given()
                .when().delete("/subjects/any-id/contents/content-id")
                .then()
                .statusCode(401);
    }
}
