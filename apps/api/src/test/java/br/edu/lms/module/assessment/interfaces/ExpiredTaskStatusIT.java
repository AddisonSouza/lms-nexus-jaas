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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Uma tarefa publicada cujo prazo venceu deve ser lida como CLOSED nos três
 * endpoints de leitura, sem sumir das listas do aluno e sem que o banco mude —
 * o status é derivado do prazo, não persistido.
 */
@QuarkusTest
class ExpiredTaskStatusIT {

    static final String ORG_ID = "53000000-5300-5300-5300-530000000001";
    static final String TEACHER_ID = "53000000-5300-5300-5300-530000000002";
    static final String STUDENT_ID = "53000000-5300-5300-5300-530000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String subjectId;
    String expiredTaskId;
    String openTaskId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor Expired IT");
        insertUser(STUDENT_ID, "Aluno Expired IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Expired Task Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        insertMember(TEACHER_ID, "PROFESSOR");
        insertMember(STUDENT_ID, "ALUNO");

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Expired IT")
                .executeUpdate();

        expiredTaskId = insertTask("Tarefa vencida", LocalDateTime.now().minusDays(1));
        openTaskId = insertTask("Tarefa no prazo", LocalDateTime.now().plusDays(7));

        // O aluno já entregou e foi avaliado na tarefa vencida: a nota tem de
        // continuar alcançável depois do prazo (RF-14).
        em.createNativeQuery("""
                        INSERT INTO task_submissions (id, task_id, student_id, organization_id, text_response, status, grade, feedback, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, expiredTaskId)
                .setParameter(3, STUDENT_ID).setParameter(4, ORG_ID).setParameter(5, "Minha resposta")
                .setParameter(6, "EVALUATED").setParameter(7, new java.math.BigDecimal("8.50"))
                .setParameter(8, "Bom trabalho")
                .executeUpdate();
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

    private String insertTask(String title, LocalDateTime deadline) {
        String id = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, max_score, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, id).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, title).setParameter(6, "Enunciado")
                .setParameter(7, deadline).setParameter(8, new java.math.BigDecimal("10.00"))
                .setParameter(9, "PUBLISHED")
                .executeUpdate();
        return id;
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)").setParameter(1, TEACHER_ID).setParameter(2, STUDENT_ID).executeUpdate();
        tx.commit();
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void listTasks_reportsTheExpiredTaskAsClosedAndKeepsTheOpenOnePublished() {
        given()
                .when().get("/tasks")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + expiredTaskId + "' }.status", equalTo("CLOSED"))
                .body("find { it.id == '" + openTaskId + "' }.status", equalTo("PUBLISHED"));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void listPublishedTasks_stillReturnsTheExpiredTaskAsClosed() {
        given()
                .when().get("/tasks/published")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + expiredTaskId + "' }.status", equalTo("CLOSED"));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void myGrades_keepsTheGradeOfTheExpiredTaskReachable() {
        given()
                .when().get("/tasks/my-grades")
                .then()
                .statusCode(200)
                .body("find { it.id == '" + expiredTaskId + "' }.status", equalTo("CLOSED"))
                .body("find { it.id == '" + expiredTaskId + "' }.submission.grade", equalTo(8.50f));
    }

    @Test
    @TestSecurity(user = STUDENT_ID, roles = {"ALUNO"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = STUDENT_ID), @Claim(key = "org", value = ORG_ID) })
    void submittingToAnExpiredTaskStillFailsWithDeadlineExpired() {
        given()
                .multiPart("textResponse", "resposta atrasada")
                .when().post("/tasks/{id}/submissions", expiredTaskId)
                .then()
                .statusCode(422)
                .body("error", equalTo("DEADLINE_EXPIRED"));
    }

    @Test
    void theDatabaseKeepsStoringPublished() {
        Object stored = em.createNativeQuery("SELECT status FROM tasks WHERE id = ?")
                .setParameter(1, expiredTaskId)
                .getSingleResult();

        assertThat(stored.toString()).isEqualTo("PUBLISHED");
    }
}
