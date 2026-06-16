package br.edu.lms.module.assessment.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class TaskResourceIT {

    @Test
    void createTask_withoutToken_returns401() {
        given()
                .when().post("/tasks")
                .then()
                .statusCode(401);
    }

    @Test
    void publishTask_withoutToken_returns401() {
        given()
                .when().patch("/tasks/any-id/publish")
                .then()
                .statusCode(401);
    }
}
