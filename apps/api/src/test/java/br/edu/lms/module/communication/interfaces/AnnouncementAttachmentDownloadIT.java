package br.edu.lms.module.communication.interfaces;

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

/** O anexo de um aviso fica restrito a quem enxerga o mural: os membros da turma. */
@QuarkusTest
class AnnouncementAttachmentDownloadIT {

    static final String ORG_ID = "62000000-6200-6200-6200-620000000001";
    static final String OTHER_ORG_ID = "62000000-6200-6200-6200-620000000009";
    static final String TEACHER_ID = "62000000-6200-6200-6200-620000000002";
    static final String STUDENT_ID = "62000000-6200-6200-6200-620000000003";
    static final String OUTSIDER_ID = "62000000-6200-6200-6200-620000000004";
    static final byte[] CONTENT = "cronograma".getBytes(StandardCharsets.UTF_8);

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject StoragePort storagePort;

    String fileKey;

    @BeforeEach
    void setUp() throws Exception {
        fileKey = storagePort.store(new ByteArrayInputStream(CONTENT), "cronograma.pdf", "application/pdf",
                CONTENT.length, StorageContext.ANNOUNCEMENT_ATTACHMENT).getFileKey();

        tx.begin();
        for (var id : new String[] { TEACHER_ID, STUDENT_ID, OUTSIDER_ID }) {
            em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                    .setParameter(1, id).setParameter(2, "Announcement Download IT").setParameter(3, id + "@test.com")
                    .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                    .executeUpdate();
        }
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Announcement Download Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        String classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma")
                .setParameter(4, "2026/2").setParameter(5, "ACTIVE").setParameter(6, "AAD001")
                .executeUpdate();
        insertClassroomMember(classroomId, TEACHER_ID, "PROFESSOR");
        insertClassroomMember(classroomId, STUDENT_ID, "ALUNO");

        String announcementId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO announcements (id, classroom_id, organization_id, author_id, content, created_at, updated_at)
                        VALUES (?,?,?,?,?,NOW(),NOW())
                        """)
                .setParameter(1, announcementId).setParameter(2, classroomId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, "Segue o cronograma")
                .executeUpdate();
        em.createNativeQuery("""
                        INSERT INTO announcement_attachments (id, announcement_id, file_key, original_name, mime_type, size_bytes, created_at)
                        VALUES (?,?,?,?,?,?,NOW())
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, announcementId).setParameter(3, fileKey)
                .setParameter(4, "cronograma.pdf").setParameter(5, "application/pdf").setParameter(6, CONTENT.length)
                .executeUpdate();
        tx.commit();
    }

    private void insertClassroomMember(String classroomId, String userId, String role) {
        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId)
                .setParameter(3, userId).setParameter(4, ORG_ID).setParameter(5, role)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE aa FROM announcement_attachments aa JOIN announcements a ON aa.announcement_id = a.id WHERE a.organization_id = ?")
                .setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM announcements WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_ID).setParameter(3, OUTSIDER_ID)
                .executeUpdate();
        tx.commit();
        storagePort.delete(fileKey);
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void aStudentOfTheClassroomDownloadsIt() {
        given().when().get("/files/{key}", fileKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void aTeacherOfTheClassroomDownloadsIt() {
        given().when().get("/files/{key}", fileKey).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = OUTSIDER_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = OUTSIDER_ID), @Claim(key = "org", value = ORG_ID) })
    void someoneOutsideTheClassroomGets404() {
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
