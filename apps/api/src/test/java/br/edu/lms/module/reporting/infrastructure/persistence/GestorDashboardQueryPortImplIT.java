package br.edu.lms.module.reporting.infrastructure.persistence;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@QuarkusTest
class GestorDashboardQueryPortImplIT {

    static final String ORG_ID = "40000000-4000-4000-4000-400000000001";
    static final String TEACHER_ID = "40000000-4000-4000-4000-400000000002";
    static final String STUDENT_ON_TRACK_ID = "40000000-4000-4000-4000-400000000003";
    static final String STUDENT_AT_RISK_ID = "40000000-4000-4000-4000-400000000004";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject GestorDashboardQueryPortImpl sut;

    String classroomId;
    String subjectId;
    String taskOnTimeId;
    String taskLateId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Teacher IT");
        insertUser(STUDENT_ON_TRACK_ID, "Aluno Em Dia");
        insertUser(STUDENT_AT_RISK_ID, "Aluno Em Risco");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Gestor Dashboard Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();

        classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma Gestor IT")
                .setParameter(4, "2026.1").setParameter(5, "ACTIVE").setParameter(6, "GES123")
                .executeUpdate();

        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_ON_TRACK_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_AT_RISK_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Gestor IT")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, classroomId)
                .executeUpdate();

        // Task with deadline yesterday — STUDENT_ON_TRACK submitted before the deadline (evaluated), STUDENT_AT_RISK never submitted.
        taskOnTimeId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,DATE_SUB(NOW(6), INTERVAL 1 DAY),?,NOW(6),NOW(6))")
                .setParameter(1, taskOnTimeId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, TEACHER_ID)
                .setParameter(5, "Tarefa em dia").setParameter(6, "Descrição").setParameter(7, "PUBLISHED")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, grade, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,DATE_SUB(NOW(6), INTERVAL 2 DAY),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskOnTimeId).setParameter(3, STUDENT_ON_TRACK_ID)
                .setParameter(4, ORG_ID).setParameter(5, "EVALUATED").setParameter(6, new BigDecimal("8.00"))
                .executeUpdate();

        // Task with deadline two days ago — STUDENT_ON_TRACK submitted late (today), STUDENT_AT_RISK never submitted.
        taskLateId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,DATE_SUB(NOW(6), INTERVAL 2 DAY),?,NOW(6),NOW(6))")
                .setParameter(1, taskLateId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, TEACHER_ID)
                .setParameter(5, "Tarefa atrasada").setParameter(6, "Descrição").setParameter(7, "PUBLISHED")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskLateId).setParameter(3, STUDENT_ON_TRACK_ID)
                .setParameter(4, ORG_ID).setParameter(5, "SUBMITTED")
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, STUDENT_ON_TRACK_ID).setParameter(3, STUDENT_AT_RISK_ID)
                .executeUpdate();
        tx.commit();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @Test
    void getClassroomsHealth_returnsDeliveryRateAndAverageGradeForClassroom() {
        var result = sut.getClassroomsHealth(ORG_ID);

        assertThat(result).hasSize(1);
        var health = result.get(0);
        assertThat(health.getClassroomId()).isEqualTo(classroomId);
        assertThat(health.getStatus()).isEqualTo("ACTIVE");
        // 2 tasks, 1 submission (any status) each out of 2 eligible students -> 0.5 average
        assertThat(health.getDeliveryRate()).isEqualByComparingTo(new BigDecimal("0.5000"));
        // Only the EVALUATED submission (grade 8.00) counts towards the average
        assertThat(health.getAverageGrade()).isEqualByComparingTo(new BigDecimal("8.00"));
    }

    @Test
    void getClassroomsHealth_classroomWithoutEvaluatedSubmissions_returnsNullAverageGrade() throws Exception {
        tx.begin();
        em.createNativeQuery("UPDATE task_submissions SET status = 'SUBMITTED', grade = NULL WHERE organization_id = ?")
                .setParameter(1, ORG_ID).executeUpdate();
        tx.commit();

        var result = sut.getClassroomsHealth(ORG_ID);

        assertThat(result.get(0).getAverageGrade()).isNull();
    }

    @Test
    void listAtRiskStudents_ranksStudentWithMorePendenciesFirst() {
        var result = sut.listAtRiskStudents(classroomId, 5);

        assertThat(result).extracting("studentId", "pendingCount")
                .containsExactly(
                        tuple(STUDENT_AT_RISK_ID, 2L),
                        tuple(STUDENT_ON_TRACK_ID, 1L));
        assertThat(result.get(0).getStudentName()).isEqualTo("Aluno Em Risco");
    }

    @Test
    void listAtRiskStudents_respectsLimit() {
        var result = sut.listAtRiskStudents(classroomId, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_AT_RISK_ID);
    }

    @Test
    void listAtRiskStudents_classroomWithNoOverdueTasks_returnsEmpty() throws Exception {
        tx.begin();
        em.createNativeQuery("UPDATE tasks SET deadline = DATE_ADD(NOW(6), INTERVAL 10 DAY) WHERE organization_id = ?")
                .setParameter(1, ORG_ID).executeUpdate();
        tx.commit();

        var result = sut.listAtRiskStudents(classroomId, 5);

        assertThat(result).isEmpty();
    }
}
