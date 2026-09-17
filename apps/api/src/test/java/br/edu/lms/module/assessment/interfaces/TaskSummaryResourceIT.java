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

import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * GET /tasks acompanha os contadores de entrega; GET /tasks/published, que o
 * aluno consome, segue sem eles.
 */
@QuarkusTest
class TaskSummaryResourceIT {

    static final String ORG_ID = "55000000-5500-5500-5500-550000000001";
    static final String TEACHER_ID = "55000000-5500-5500-5500-550000000002";
    static final String STUDENT_A = "55000000-5500-5500-5500-550000000003";
    static final String STUDENT_B = "55000000-5500-5500-5500-550000000004";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String subjectId;
    String taskComPendentes;
    String taskSemRespostas;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor Summary IT");
        insertUser(STUDENT_A, "Aluno A Summary IT");
        insertUser(STUDENT_B, "Aluno B Summary IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Summary Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        insertMember(TEACHER_ID, "PROFESSOR");
        insertMember(STUDENT_A, "ALUNO");

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Summary IT")
                .executeUpdate();

        taskComPendentes = insertTask("Tarefa com respostas");
        taskSemRespostas = insertTask("Tarefa sem respostas");

        // Duas entregas, uma já avaliada: 2 recebidas, 1 pendente.
        insertSubmission(taskComPendentes, STUDENT_A, "SUBMITTED");
        insertSubmission(taskComPendentes, STUDENT_B, "EVALUATED");
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    private void insertMember(String userId, String role) {
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, ORG_ID)
                .setParameter(3, userId).setParameter(4, role)
                .executeUpdate();
    }

    private String insertTask(String title) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, id).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, title).setParameter(6, "Enunciado")
                .setParameter(7, LocalDateTime.now().plusDays(7)).setParameter(8, "PUBLISHED")
                .executeUpdate();
        return id;
    }

    private void insertSubmission(String taskId, String studentId, String status) {
        em.createNativeQuery("""
                        INSERT INTO task_submissions (id, task_id, student_id, organization_id, text_response, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskId)
                .setParameter(3, studentId).setParameter(4, ORG_ID).setParameter(5, "Resposta")
                .setParameter(6, status)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_A).setParameter(3, STUDENT_B).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void listTasks_reportsReceivedAndPendingCounts() {
        given()
                .when().get("/tasks")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + taskComPendentes + "' }.submissionCount", equalTo(2))
                .body("find { it.id == '" + taskComPendentes + "' }.pendingEvaluationCount", equalTo(1));
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void listTasks_zeroesATaskWithoutSubmissionsInsteadOfOmittingIt() {
        given()
                .when().get("/tasks")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + taskSemRespostas + "' }.submissionCount", equalTo(0))
                .body("find { it.id == '" + taskSemRespostas + "' }.pendingEvaluationCount", equalTo(0));
    }

    @Test
    @TestSecurity(user = STUDENT_A, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_A), @Claim(key = "org", value = ORG_ID) })
    void publishedTasks_doNotLeakTheCountsToTheStudent() {
        given()
                .when().get("/tasks/published")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + taskComPendentes + "' }.submissionCount", nullValue())
                .body("find { it.id == '" + taskComPendentes + "' }.pendingEvaluationCount", nullValue());
    }
}
