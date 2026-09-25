package br.edu.lms.module.assessment.interfaces;

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
 * A entrega de um aluno só é baixada por ele e pelo professor que criou a
 * tarefa — nem colegas, nem outros professores.
 */
@QuarkusTest
class SubmissionAttachmentDownloadIT {

    static final String ORG_ID = "61000000-6100-6100-6100-610000000001";
    static final String OTHER_ORG_ID = "61000000-6100-6100-6100-610000000009";
    static final String TEACHER_ID = "61000000-6100-6100-6100-610000000002";
    static final String OTHER_TEACHER_ID = "61000000-6100-6100-6100-610000000003";
    static final String STUDENT_ID = "61000000-6100-6100-6100-610000000004";
    static final String CLASSMATE_ID = "61000000-6100-6100-6100-610000000005";
    static final byte[] CONTENT = "resposta do aluno".getBytes(StandardCharsets.UTF_8);

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject StoragePort storagePort;

    String fileKey;

    @BeforeEach
    void setUp() throws Exception {
        fileKey = storagePort.store(new ByteArrayInputStream(CONTENT), "resposta.pdf", "application/pdf",
                CONTENT.length, StorageContext.SUBMISSION_ATTACHMENT).getFileKey();

        tx.begin();
        for (var id : new String[] { TEACHER_ID, OTHER_TEACHER_ID, STUDENT_ID, CLASSMATE_ID }) {
            em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                    .setParameter(1, id).setParameter(2, "Submission Download IT").setParameter(3, id + "@test.com")
                    .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                    .executeUpdate();
        }
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Submission Download Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        String subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina")
                .executeUpdate();
        String taskId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,DATE_ADD(NOW(), INTERVAL 7 DAY),'PUBLISHED',NOW(),NOW())
                        """)
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, "Tarefa").setParameter(6, "Descrição")
                .executeUpdate();
        String submissionId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, created_at, updated_at)
                        VALUES (?,?,?,?,'SUBMITTED',NOW(),NOW())
                        """)
                .setParameter(1, submissionId).setParameter(2, taskId).setParameter(3, STUDENT_ID).setParameter(4, ORG_ID)
                .executeUpdate();
        em.createNativeQuery("""
                        INSERT INTO submission_attachments (id, submission_id, file_key, original_name, mime_type, size_bytes, created_at)
                        VALUES (?,?,?,?,?,?,NOW())
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, submissionId).setParameter(3, fileKey)
                .setParameter(4, "resposta.pdf").setParameter(5, "application/pdf").setParameter(6, CONTENT.length)
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE a FROM submission_attachments a JOIN task_submissions s ON a.submission_id = s.id WHERE s.organization_id = ?")
                .setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, OTHER_TEACHER_ID)
                .setParameter(3, STUDENT_ID).setParameter(4, CLASSMATE_ID)
                .executeUpdate();
        tx.commit();
        storagePort.delete(fileKey);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void theStudentWhoSubmittedDownloadsIt() {
        given().when().get("/files/{key}", fileKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = CLASSMATE_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = CLASSMATE_ID), @Claim(key = "org", value = ORG_ID) })
    void aClassmateGets404() {
        given().when().get("/files/{key}", fileKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void theTeacherWhoCreatedTheTaskDownloadsIt() {
        given().when().get("/files/{key}", fileKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = OTHER_TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OTHER_TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void anotherTeacherGets404() {
        given().when().get("/files/{key}", fileKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = OTHER_ORG_ID) })
    void theSameStudentUnderAnotherOrganizationGets404() {
        given().when().get("/files/{key}", fileKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }
}
