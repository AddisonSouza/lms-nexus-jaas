package br.edu.lms.module.curriculum.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class SubjectResourceIT {

    @Test
    void createSubject_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Matemática"}
                        """)
                .when().post("/subjects")
                .then()
                .statusCode(401);
    }

    @Test
    void listSubjects_withoutToken_returns401() {
        given()
                .when().get("/subjects")
                .then()
                .statusCode(401);
    }

    @Test
    void getSubject_withoutToken_returns401() {
        given()
                .when().get("/subjects/some-id")
                .then()
                .statusCode(401);
    }

    @Test
    void updateSubject_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Física"}
                        """)
                .when().put("/subjects/some-id")
                .then()
                .statusCode(401);
    }

    @Test
    void deleteSubject_withoutToken_returns401() {
        given()
                .when().delete("/subjects/some-id")
                .then()
                .statusCode(401);
    }

    @Test
    void linkClassroom_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"classroomId":"cls-1"}
                        """)
                .when().post("/subjects/some-id/classrooms")
                .then()
                .statusCode(401);
    }

    @Test
    void unlinkClassroom_withoutToken_returns401() {
        given()
                .when().delete("/subjects/some-id/classrooms/cls-1")
                .then()
                .statusCode(401);
    }

    @Test
    void assignTeacher_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"memberId":"mem-1"}
                        """)
                .when().post("/subjects/some-id/teachers")
                .then()
                .statusCode(401);
    }

    @Test
    void removeTeacher_withoutToken_returns401() {
        given()
                .when().delete("/subjects/some-id/teachers/mem-1")
                .then()
                .statusCode(401);
    }

    @Test
    void createSubject_withMissingName_returns401WithoutToken() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"code":"MAT101"}
                        """)
                .when().post("/subjects")
                .then()
                .statusCode(401);
    }
}
