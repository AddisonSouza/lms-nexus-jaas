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

@QuarkusTest
class StudentDashboardQueryPortImplIT {

    static final String ORG_ID = "42000000-4200-4200-4200-420000000001";
    static final String STUDENT_ID = "42000000-4200-4200-4200-420000000002";
    static final String OTHER_STUDENT_ID = "42000000-4200-4200-4200-420000000003";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject StudentDashboardQueryPortImpl sut;

    String classroomId;
    String subjectAId;
    String subjectBId;
    String taskNearDeadlineId;
    String taskFarDeadlineId;
    String taskEvaluatedAId;
    String taskEvaluatedBId;
    String taskDraftId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(STUDENT_ID, "Aluno Dashboard IT");
        insertUser(OTHER_STUDENT_ID, "Outro Aluno IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Student Dashboard Test Org").setParameter(3, STUDENT_ID)
                .executeUpdate();

        classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma Aluno IT")
                .setParameter(4, "2026.1").setParameter(5, "ACTIVE").setParameter(6, "ALU123")
                .executeUpdate();

        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();

        subjectAId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectAId).setParameter(2, ORG_ID).setParameter(3, "Disciplina A")
                .executeUpdate();
        subjectBId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectBId).setParameter(2, ORG_ID).setParameter(3, "Disciplina B")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectAId).setParameter(2, classroomId)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectBId).setParameter(2, classroomId)
                .executeUpdate();

        // Pending tasks (published, no submission from STUDENT_ID), different deadlines to assert urgency ordering.
        taskFarDeadlineId = insertTask(subjectAId, "Tarefa prazo distante", "DATE_ADD(NOW(6), INTERVAL 10 DAY)", "PUBLISHED");
        taskNearDeadlineId = insertTask(subjectAId, "Tarefa prazo próximo", "DATE_ADD(NOW(6), INTERVAL 2 DAY)", "PUBLISHED");

        // Evaluated task in subject A — submission updated further in the past (less recent).
        taskEvaluatedAId = insertTask(subjectAId, "Tarefa avaliada A", "DATE_ADD(NOW(6), INTERVAL 1 DAY)", "PUBLISHED");
        insertSubmission(taskEvaluatedAId, STUDENT_ID, "EVALUATED", new BigDecimal("7.00"), "Bom trabalho",
                "DATE_SUB(NOW(6), INTERVAL 1 DAY)");

        // Evaluated task in subject B — submission updated more recently.
        taskEvaluatedBId = insertTask(subjectBId, "Tarefa avaliada B", "DATE_ADD(NOW(6), INTERVAL 5 DAY)", "PUBLISHED");
        insertSubmission(taskEvaluatedBId, STUDENT_ID, "EVALUATED", new BigDecimal("9.00"), "Excelente", "NOW(6)");

        // Draft task — must never be considered, regardless of submissions.
        taskDraftId = insertTask(subjectAId, "Tarefa rascunho", "DATE_ADD(NOW(6), INTERVAL 1 DAY)", "DRAFT");

        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id IN (?,?)")
                .setParameter(1, subjectAId).setParameter(2, subjectBId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, STUDENT_ID).setParameter(2, OTHER_STUDENT_ID).executeUpdate();
        tx.commit();
    }

    private String insertTask(String subjectId, String title, String deadlineExpr, String status) {
        String taskId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?," + deadlineExpr + ",?,NOW(6),NOW(6))")
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, STUDENT_ID)
                .setParameter(5, title).setParameter(6, "Descrição").setParameter(7, status)
                .executeUpdate();
        return taskId;
    }

    private void insertSubmission(String taskId, String studentId, String status, BigDecimal grade, String feedback,
                                   String updatedAtExpr) {
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, grade, feedback, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,?,NOW(6)," + updatedAtExpr + ")")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskId).setParameter(3, studentId)
                .setParameter(4, ORG_ID).setParameter(5, status).setParameter(6, grade).setParameter(7, feedback)
                .executeUpdate();
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id).setParameter(2, name).setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder").setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @Test
    void getUpcomingPendingTasks_returnsOnlyPublishedTasksWithoutSubmission_orderedByDeadlineAsc() {
        var result = sut.getUpcomingPendingTasks(STUDENT_ID, ORG_ID);

        assertThat(result).extracting("taskId").containsExactly(taskNearDeadlineId, taskFarDeadlineId);
    }

    @Test
    void getUpcomingPendingTasks_studentWithoutAnyClassroom_returnsEmpty() {
        var result = sut.getUpcomingPendingTasks(OTHER_STUDENT_ID, ORG_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void countPendingTasks_countsPublishedTasksWithoutSubmission() {
        assertThat(sut.countPendingTasks(STUDENT_ID, ORG_ID)).isEqualTo(2L);
    }

    @Test
    void countSubmittedTasks_countsPublishedTasksWithSubmission() {
        assertThat(sut.countSubmittedTasks(STUDENT_ID, ORG_ID)).isEqualTo(2L);
    }

    @Test
    void getRecentGrades_returnsEvaluatedSubmissionsOrderedByMostRecentFirst() {
        var result = sut.getRecentGrades(STUDENT_ID, ORG_ID);

        assertThat(result).extracting("taskId").containsExactly(taskEvaluatedBId, taskEvaluatedAId);
        assertThat(result).extracting("grade").containsExactly(new BigDecimal("9.00"), new BigDecimal("7.00"));
    }

    @Test
    void getRecentGrades_limitsResultToFiveMostRecent() throws Exception {
        tx.begin();
        for (int i = 0; i < 5; i++) {
            String extraTaskId = insertTask(subjectAId, "Tarefa extra " + i, "DATE_ADD(NOW(6), INTERVAL 1 DAY)", "PUBLISHED");
            insertSubmission(extraTaskId, STUDENT_ID, "EVALUATED", new BigDecimal("6.00"), "Feedback " + i,
                    "DATE_ADD(NOW(6), INTERVAL " + i + " MINUTE)");
        }
        tx.commit();

        var result = sut.getRecentGrades(STUDENT_ID, ORG_ID);

        assertThat(result).hasSize(5);
    }

    @Test
    void getAverageGradePerSubject_groupsEvaluatedGradesBySubject() {
        var result = sut.getAverageGradePerSubject(STUDENT_ID, ORG_ID);

        assertThat(result).hasSize(2);
        assertThat(result).filteredOn(s -> s.getSubjectId().equals(subjectAId))
                .extracting("averageGrade").containsExactly(new BigDecimal("7.00"));
        assertThat(result).filteredOn(s -> s.getSubjectId().equals(subjectBId))
                .extracting("averageGrade").containsExactly(new BigDecimal("9.00"));
    }

    @Test
    void getAverageGradePerSubject_studentWithoutEvaluatedSubmissions_returnsEmpty() {
        var result = sut.getAverageGradePerSubject(OTHER_STUDENT_ID, ORG_ID);

        assertThat(result).isEmpty();
    }
}
