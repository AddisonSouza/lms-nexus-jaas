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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * O anexo de uma tarefa segue a visibilidade da própria tarefa: o aluno só o
 * alcança depois de publicada, e ninguém de outra organização o alcança.
 */
@QuarkusTest
class TaskAttachmentDownloadIT {

    static final String ORG_ID = "60000000-6000-6000-6000-600000000001";
    static final String OTHER_ORG_ID = "60000000-6000-6000-6000-600000000009";
    static final String TEACHER_ID = "60000000-6000-6000-6000-600000000002";
    static final String STUDENT_ID = "60000000-6000-6000-6000-600000000003";
    static final byte[] CONTENT = "enunciado da prova".getBytes(StandardCharsets.UTF_8);

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject StoragePort storagePort;

    String publishedKey;
    String draftKey;

    @BeforeEach
    void setUp() throws Exception {
        publishedKey = store();
        draftKey = store();

        tx.begin();
        insertUser(TEACHER_ID);
        insertUser(STUDENT_ID);
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Task Download Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        String subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina")
                .executeUpdate();
        insertTaskWithAttachment(subjectId, "PUBLISHED", publishedKey);
        insertTaskWithAttachment(subjectId, "DRAFT", draftKey);
        tx.commit();
    }

    private String store() {
        return storagePort.store(new ByteArrayInputStream(CONTENT), "prova final.pdf", "application/pdf",
                CONTENT.length, StorageContext.TASK_ATTACHMENT).getFileKey();
    }

    private void insertUser(String id) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, "Task Download IT").setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertTaskWithAttachment(String subjectId, String status, String fileKey) {
        String taskId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,DATE_ADD(NOW(), INTERVAL 7 DAY),?,NOW(),NOW())
                        """)
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, "Tarefa " + status).setParameter(6, "Descrição")
                .setParameter(7, status)
                .executeUpdate();
        em.createNativeQuery("""
                        INSERT INTO task_attachments (id, task_id, file_key, original_name, mime_type, size_bytes, created_at)
                        VALUES (?,?,?,?,?,?,NOW())
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskId).setParameter(3, fileKey)
                .setParameter(4, "prova final.pdf").setParameter(5, "application/pdf").setParameter(6, CONTENT.length)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE a FROM task_attachments a JOIN tasks t ON a.task_id = t.id WHERE t.organization_id = ?")
                .setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_ID).executeUpdate();
        tx.commit();
        storagePort.delete(publishedKey);
        storagePort.delete(draftKey);
    }

    // Sem o tipo e o nome originais o navegador salva um `application/octet-stream`
    // batizado com a chave, que é um UUID sem extensão.
    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentDownloadsTheAttachmentOfAPublishedTask_withItsRealTypeNameAndBytes() {
        var body = given()
                .when().get("/files/{key}", publishedKey)
                .then()
                .statusCode(200)
                .contentType("application/pdf")
                .header("Content-Disposition", allOf(
                        startsWith("attachment;"),
                        containsString("filename=\"prova_final.pdf\""),
                        containsString("filename*=UTF-8''prova%20final.pdf")))
                .extract().asByteArray();

        assertThat(body).isEqualTo(CONTENT);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void studentGets404OnTheAttachmentOfADraft() {
        given().when().get("/files/{key}", draftKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void teacherDownloadsTheAttachmentOfADraft() {
        given().when().get("/files/{key}", draftKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"ADMIN_ORG"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = OTHER_ORG_ID) })
    void anotherOrganizationGets404() {
        given().when().get("/files/{key}", publishedKey)
                .then().statusCode(404).body("error", equalTo("FILE_NOT_FOUND"));
    }
}
