package br.edu.lms.module.assessment.interfaces;

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

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Publishing a task must answer with the persisted task — audit fields included.
 * A null createdAt in the response is what made the web client discard it and
 * keep offering the Publicar button (#252).
 */
@QuarkusTest
class PublishTaskResponseIT {

    static final String ORG_ID = "52000000-5200-5200-5200-520000000001";
    static final String TEACHER_ID = "52000000-5200-5200-5200-520000000002";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String subjectId;
    String taskId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, "Professor Publish IT").setParameter(3, TEACHER_ID + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Publish Task Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, ORG_ID)
                .setParameter(3, TEACHER_ID).setParameter(4, "PROFESSOR")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Publish IT")
                .executeUpdate();

        taskId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, "Tarefa a publicar")
                .setParameter(6, "Enunciado").setParameter(7, java.time.LocalDateTime.now().plusDays(7))
                .setParameter(8, "DRAFT")
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id = ?").setParameter(1, TEACHER_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void publishTask_returnsPublishedTaskWithAuditFields() {
        given()
                .when().patch("/tasks/{id}/publish", taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId))
                .body("status", equalTo("PUBLISHED"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }
}
