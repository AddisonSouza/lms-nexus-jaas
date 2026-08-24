package br.edu.lms.module.classroom.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ClassroomResourceIT {

    @Test
    void createClassroom_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Turma A","academicPeriod":"2025/1"}
                        """)
                .when().post("/classrooms")
                .then()
                .statusCode(401);
    }

    @Test
    void listClassrooms_withoutToken_returns401() {
        given()
                .when().get("/classrooms")
                .then()
                .statusCode(401);
    }

    @Test
    void getClassroom_withoutToken_returns401() {
        given()
                .when().get("/classrooms/some-id")
                .then()
                .statusCode(401);
    }

    @Test
    void deleteClassroom_withoutToken_returns401() {
        given()
                .when().delete("/classrooms/some-id")
                .then()
                .statusCode(401);
    }

    @Test
    void joinClassroom_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode":"ABC123"}
                        """)
                .when().post("/classrooms/join")
                .then()
                .statusCode(401);
    }
}
