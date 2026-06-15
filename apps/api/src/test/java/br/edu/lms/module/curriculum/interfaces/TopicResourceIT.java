package br.edu.lms.module.curriculum.interfaces;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class TopicResourceIT {

    @Test
    void listTopics_withoutToken_returns401() {
        given()
                .when().get("/subjects/any-id/topics")
                .then()
                .statusCode(401);
    }

    @Test
    void createTopic_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"title":"Unidade 1"}
                        """)
                .when().post("/subjects/any-id/topics")
                .then()
                .statusCode(401);
    }

    @Test
    void updateTopic_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"title":"Unidade 2"}
                        """)
                .when().put("/subjects/any-id/topics/topic-id")
                .then()
                .statusCode(401);
    }

    @Test
    void deleteTopic_withoutToken_returns401() {
        given()
                .when().delete("/subjects/any-id/topics/topic-id")
                .then()
                .statusCode(401);
    }

    @Test
    void reorderTopics_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"topicIds":["id1","id2"]}
                        """)
                .when().put("/subjects/any-id/topics/reorder")
                .then()
                .statusCode(401);
    }
}
