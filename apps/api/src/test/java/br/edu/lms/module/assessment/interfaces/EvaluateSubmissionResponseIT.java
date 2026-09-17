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
import static org.hamcrest.Matchers.*;

/**
 * A resposta de avaliar uma submissão precisa sair com os timestamps que o banco
 * guardou. Eles saíam nulos, o Zod do front recusava a resposta inteira, a
 * mutation caía em erro e o diálogo nunca fechava — mesmo com a nota salva.
 */
@QuarkusTest
class EvaluateSubmissionResponseIT {

    static final String ORG_ID = "54000000-5400-5400-5400-540000000001";
    static final String TEACHER_ID = "54000000-5400-5400-5400-540000000002";
    static final String STUDENT_ID = "54000000-5400-5400-5400-540000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    String submissionId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor Evaluate IT");
        insertUser(STUDENT_ID, "Aluno Evaluate IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Evaluate Response Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();
        insertMember(TEACHER_ID, "PROFESSOR");
        insertMember(STUDENT_ID, "ALUNO");

        var subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Evaluate IT")
                .executeUpdate();

        var taskId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, max_score, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID)
                .setParameter(4, TEACHER_ID).setParameter(5, "Tarefa a avaliar").setParameter(6, "Enunciado")
                .setParameter(7, LocalDateTime.now().plusDays(7)).setParameter(8, new java.math.BigDecimal("10.00"))
                .setParameter(9, "PUBLISHED")
                .executeUpdate();

        submissionId = UUID.randomUUID().toString();
        em.createNativeQuery("""
                        INSERT INTO task_submissions (id, task_id, student_id, organization_id, text_response, status, created_at, updated_at)
                        VALUES (?,?,?,?,?,?,NOW(6),NOW(6))
                        """)
                .setParameter(1, submissionId).setParameter(2, taskId).setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID).setParameter(5, "Minha resposta").setParameter(6, "SUBMITTED")
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
        em.createNativeQuery("INSERT IGNORE INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, ORG_ID)
                .setParameter(3, userId).setParameter(4, role)
                .executeUpdate();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        // Avaliar dispara o evento que cria a notificação do aluno: ela referencia
        // a organização e precisa sair antes dela.
        em.createNativeQuery("DELETE FROM notifications WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
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
    void evaluate_returnsTheGradeAndTheTimestampsTheDatabaseKept() {
        given()
                .contentType("application/json")
                .body("{\"grade\":8.5,\"feedback\":\"Bom trabalho\"}")
                .when().patch("/submissions/" + submissionId + "/evaluation")
                .then()
                .statusCode(200)
                .body("id", equalTo(submissionId))
                .body("status", equalTo("EVALUATED"))
                .body("grade", equalTo(8.5f))
                .body("feedback", equalTo("Bom trabalho"))
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    @TestSecurity(user = TEACHER_ID, roles = {"PROFESSOR"})
    @JwtSecurity(claims = { @Claim(key = "sub", value = TEACHER_ID), @Claim(key = "org", value = ORG_ID) })
    void evaluate_keepsTheOriginalCreatedAtInTheDatabase() throws Exception {
        var before = createdAtInDatabase();

        given()
                .contentType("application/json")
                .body("{\"grade\":7.0,\"feedback\":\"Ok\"}")
                .when().patch("/submissions/" + submissionId + "/evaluation")
                .then()
                .statusCode(200);

        org.junit.jupiter.api.Assertions.assertEquals(before, createdAtInDatabase(),
                "avaliar não pode reescrever a data de entrega do aluno");
    }

    private Object createdAtInDatabase() throws Exception {
        tx.begin();
        var value = em.createNativeQuery("SELECT created_at FROM task_submissions WHERE id = ?")
                .setParameter(1, submissionId).getSingleResult();
        tx.commit();
        return value;
    }
}
