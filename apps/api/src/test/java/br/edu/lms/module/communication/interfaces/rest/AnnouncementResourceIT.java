package br.edu.lms.module.communication.interfaces.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AnnouncementResourceIT {

    static final String ORG_ID = "55555555-5555-5555-5555-555555555555";
    static final String CLASSROOM_ID = "66666666-6666-6666-6666-666666666666";
    static final String PROFESSOR_ID = "77777777-7777-7777-7777-777777777777";
    static final String OTHER_PROFESSOR_ID = "88888888-8888-8888-8888-888888888888";
    static final String STUDENT_ID = "99999999-9999-9999-9999-999999999999";
    static final String OUTSIDER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(PROFESSOR_ID, "Professor IT");
        insertUser(OTHER_PROFESSOR_ID, "Outro Professor IT");
        insertUser(STUDENT_ID, "Aluno IT");
        insertUser(OUTSIDER_ID, "Outsider IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID)
                .setParameter(2, "IT Test Org")
                .setParameter(3, PROFESSOR_ID)
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO classrooms (id, organization_id, name, academic_period, invite_code) VALUES (?,?,?,?,?)")
                .setParameter(1, CLASSROOM_ID)
                .setParameter(2, ORG_ID)
                .setParameter(3, "Turma IT")
                .setParameter(4, "2026.1")
                .setParameter(5, "ANC001")
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO classroom_members (id, classroom_id, user_id, organization_id, role) VALUES (?,?,?,?,?)")
                .setParameter(1, "cm-prof-1")
                .setParameter(2, CLASSROOM_ID)
                .setParameter(3, PROFESSOR_ID)
                .setParameter(4, ORG_ID)
                .setParameter(5, "PROFESSOR")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO classroom_members (id, classroom_id, user_id, organization_id, role) VALUES (?,?,?,?,?)")
                .setParameter(1, "cm-student-1")
                .setParameter(2, CLASSROOM_ID)
                .setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID)
                .setParameter(5, "ALUNO")
                .executeUpdate();
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, name)
                .setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM notifications WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM announcement_attachments WHERE announcement_id IN (SELECT id FROM announcements WHERE classroom_id = ?)")
                .setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM announcements WHERE classroom_id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE classroom_id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?,?)")
                .setParameter(1, PROFESSOR_ID).setParameter(2, OTHER_PROFESSOR_ID)
                .setParameter(3, STUDENT_ID).setParameter(4, OUTSIDER_ID)
                .executeUpdate();
        tx.commit();
    }

    // --- POST /classrooms/{classroomId}/announcements ---

    @Test
    void create_withoutAuth_returns401() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Aviso")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(401);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID)})
    void create_asStudent_returns403() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Aviso")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = OUTSIDER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = OUTSIDER_ID), @Claim(key = "org", value = ORG_ID)})
    void create_professorNotMember_returns403() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Aviso")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void create_validRequest_returns201() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Aviso importante para a turma")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then()
                .statusCode(201)
                .body("content", equalTo("Aviso importante para a turma"))
                .body("authorId", equalTo(PROFESSOR_ID));
    }

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void create_withoutContent_returns422() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("externalUrl", "https://example.com")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(422);
    }

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void create_withLinkAttachment_returns201() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Aviso com link")
                .multiPart("externalUrl", "https://example.com/material")
                .multiPart("linkTitle", "Material complementar")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then()
                .statusCode(201)
                .body("attachments[0].externalUrl", equalTo("https://example.com/material"));
    }

    // --- GET /classrooms/{classroomId}/announcements ---

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID)})
    void list_asStudentMember_returns200Empty() {
        given()
                .when().get("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @TestSecurity(user = OUTSIDER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = OUTSIDER_ID), @Claim(key = "org", value = ORG_ID)})
    void list_userNotMember_returns403() {
        given()
                .when().get("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void list_afterPosting_returnsAnnouncementOrderedDesc() {
        given().contentType(ContentType.MULTIPART).multiPart("content", "Primeiro aviso")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID).then().statusCode(201);
        given().contentType(ContentType.MULTIPART).multiPart("content", "Segundo aviso")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID).then().statusCode(201);

        given()
                .when().get("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0].content", equalTo("Segundo aviso"))
                .body("[1].content", equalTo("Primeiro aviso"));
    }

    // --- PUT /announcements/{id} ---

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void update_byAuthor_returns200() {
        var id = given().contentType(ContentType.MULTIPART).multiPart("content", "Original")
                .when().post("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(201).extract().path("id").toString();

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Atualizado")
                .when().put("/announcements/{id}", id)
                .then()
                .statusCode(200)
                .body("content", equalTo("Atualizado"))
                .body("createdAt", notNullValue());
    }

    @Test
    @TestSecurity(user = OTHER_PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = OTHER_PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void update_byNonAuthor_returns403() throws Exception {
        var id = createAsProfessor("Original");

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Tentativa de edição")
                .when().put("/announcements/{id}", id)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void update_nonExistentAnnouncement_returns404() {
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("content", "Atualizado")
                .when().put("/announcements/{id}", "nonexistent-id")
                .then().statusCode(404);
    }

    // --- DELETE /announcements/{id} ---

    @Test
    @TestSecurity(user = PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void delete_byAuthor_returns204() throws Exception {
        var id = createAsProfessor("Para excluir");

        given()
                .when().delete("/announcements/{id}", id)
                .then().statusCode(204);

        given()
                .when().get("/classrooms/{id}/announcements", CLASSROOM_ID)
                .then().statusCode(200).body("$", hasSize(0));
    }

    @Test
    @TestSecurity(user = OTHER_PROFESSOR_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = OTHER_PROFESSOR_ID), @Claim(key = "org", value = ORG_ID)})
    void delete_byNonAuthor_returns403() throws Exception {
        var id = createAsProfessor("Não pode excluir");

        given()
                .when().delete("/announcements/{id}", id)
                .then().statusCode(403);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = {@Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID)})
    void delete_asStudent_returns403() throws Exception {
        var id = createAsProfessor("Aluno não exclui");

        given()
                .when().delete("/announcements/{id}", id)
                .then().statusCode(403);
    }

    private String createAsProfessor(String content) throws Exception {
        var id = java.util.UUID.randomUUID().toString();
        tx.begin();
        em.createNativeQuery("INSERT INTO announcements (id, classroom_id, organization_id, author_id, content, created_at, updated_at) VALUES (?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, id)
                .setParameter(2, CLASSROOM_ID)
                .setParameter(3, ORG_ID)
                .setParameter(4, PROFESSOR_ID)
                .setParameter(5, content)
                .executeUpdate();
        tx.commit();
        return id;
    }
}
