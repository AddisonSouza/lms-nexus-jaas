package br.edu.lms.module.assessment.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class SubmissionResourceIT {

    @Test
    void evaluateSubmission_withoutToken_returns401() {
        given()
                .contentType("application/json")
                .body("{\"feedback\":\"ok\"}")
                .when().patch("/submissions/any-id/evaluation")
                .then()
                .statusCode(401);
    }

    @Test
    void listSubmissions_withoutToken_returns401() {
        given()
                .when().get("/tasks/any-task-id/submissions")
                .then()
                .statusCode(401);
    }
}
