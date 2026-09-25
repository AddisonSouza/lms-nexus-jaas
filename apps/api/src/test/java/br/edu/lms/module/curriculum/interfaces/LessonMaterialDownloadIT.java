package br.edu.lms.module.curriculum.interfaces;

import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * O material de uma disciplina se baixa com a mesma regra com que se lista: o
 * aluno precisa estar numa turma da disciplina, e ninguém de fora da
 * organização o alcança.
 */
@QuarkusTest
class LessonMaterialDownloadIT {

    static final String ORG_ID = "59000000-5900-5900-5900-590000000001";
    static final String OTHER_ORG_ID = "59000000-5900-5900-5900-590000000009";
    static final String TEACHER_ID = "59000000-5900-5900-5900-590000000002";
    static final String STUDENT_ID = "59000000-5900-5900-5900-590000000003";
    static final String OUTSIDER_STUDENT_ID = "59000000-5900-5900-5900-590000000004";
    static final byte[] CONTENT = "material da aula".getBytes(StandardCharsets.UTF_8);

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject StoragePort storagePort;

    String fileKey;
    String subjectId;

    @BeforeEach
    void setUp() throws Exception {
        fileKey = storagePort.store(new ByteArrayInputStream(CONTENT), "aula 1.pdf", "application/pdf",
                CONTENT.length, StorageContext.LESSON_MATERIAL).getFileKey();

        tx.begin();
        insertUser(TEACHER_ID);
        insertUser(STUDENT_ID);
        insertUser(OUTSIDER_STUDENT_ID);
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Lesson Download Org").setParameter(3, TEACHER_ID)
                .executeUpdate();

        String classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma")
                .setParameter(4, "2026/2").setParameter(5, "ACTIVE").setParameter(6, "LMD001")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId)
                .setParameter(3, STUDENT_ID).setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, classroomId)
                .executeUpdate();

        String topicId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subject_topics (id, subject_id, organization_id, title, position) VALUES (?,?,?,?,1)")
                .setParameter(1, topicId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, "Tópico")
                .executeUpdate();
        em.createNativeQuery("""
                        INSERT INTO subject_contents (id, topic_id, organization_id, title, content_type, file_key, position)
                        VALUES (?,?,?,?,?,?,1)
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, topicId).setParameter(3, ORG_ID)
                .setParameter(4, "Aula 1").setParameter(5, "DOCUMENTO").setParameter(6, fileKey)
                .executeUpdate();
        tx.commit();
    }

    private void insertUser(String id) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, "Lesson Download IT").setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM subject_contents WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_topics WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_ID).setParameter(3, OUTSIDER_STUDENT_ID)
                .executeUpdate();
        tx.commit();
        storagePort.delete(fileKey);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentOfALinkedClassroomDownloadsTheMaterial() {
        given().when().get("/files/{key}", fileKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = OUTSIDER_STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OUTSIDER_STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentOutsideTheSubjectClassroomsGets404() {
        given().when().get("/files/{key}", fileKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void teacherOfTheOrganizationDownloadsTheMaterial() {
        given().when().get("/files/{key}", fileKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = OTHER_ORG_ID) })
    void anotherOrganizationGets404() {
        given().when().get("/files/{key}", fileKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }
}
