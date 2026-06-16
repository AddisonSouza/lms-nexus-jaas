package br.edu.lms.module.assessment.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
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

    @Test
    void getFeedback_withoutToken_returns401() {
        given()
                .when().get("/submissions/any-id/feedback")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = "student-1", roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "student-1"), @Claim(key = "org", value = "org-1") })
    void evaluateSubmission_withStudentRole_returns403() {
        given()
                .contentType("application/json")
                .body("{\"feedback\":\"ok\"}")
                .when().patch("/submissions/any-id/evaluation")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "prof-1", roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "prof-1"), @Claim(key = "org", value = "org-1") })
    void getFeedback_withProfessorRole_returns403() {
        given()
                .when().get("/submissions/any-id/feedback")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "student-1", roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "student-1"), @Claim(key = "org", value = "org-1") })
    void listSubmissions_withStudentRole_returns403() {
        given()
                .when().get("/tasks/any-task-id/submissions")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "prof-1", roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "prof-1"), @Claim(key = "org", value = "org-1") })
    void evaluateSubmission_notFound_returns404() {
        given()
                .contentType("application/json")
                .body("{\"grade\": null, \"feedback\": \"Bom trabalho\"}")
                .when().patch("/submissions/nonexistent-id/evaluation")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "prof-1", roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "prof-1"), @Claim(key = "org", value = "org-1") })
    void listSubmissions_taskNotFound_returns404() {
        given()
                .when().get("/tasks/nonexistent-task/submissions")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "student-1", roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = "student-1"), @Claim(key = "org", value = "org-1") })
    void getFeedback_submissionNotFound_returns404() {
        given()
                .when().get("/submissions/nonexistent-id/feedback")
                .then()
                .statusCode(404);
    }
}
