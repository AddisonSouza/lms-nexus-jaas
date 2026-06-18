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
class ProfessorDashboardQueryPortImplIT {

    static final String ORG_ID = "41000000-4100-4100-4100-410000000001";
    static final String TEACHER_ID = "41000000-4100-4100-4100-410000000002";
    static final String OTHER_TEACHER_ID = "41000000-4100-4100-4100-410000000003";
    static final String STUDENT_A_ID = "41000000-4100-4100-4100-410000000004";
    static final String STUDENT_B_ID = "41000000-4100-4100-4100-410000000005";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject ProfessorDashboardQueryPortImpl sut;

    String classroomId;
    String subjectId;
    String teacherMemberId;
    String taskOlderId;
    String taskNewerId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(TEACHER_ID, "Professor IT");
        insertUser(OTHER_TEACHER_ID, "Outro Professor IT");
        insertUser(STUDENT_A_ID, "Aluno A");
        insertUser(STUDENT_B_ID, "Aluno B");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Professor Dashboard Test Org").setParameter(3, TEACHER_ID)
                .executeUpdate();

        teacherMemberId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, teacherMemberId).setParameter(2, ORG_ID).setParameter(3, TEACHER_ID).setParameter(4, "PROFESSOR")
                .executeUpdate();

        classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma Professor IT")
                .setParameter(4, "2026.1").setParameter(5, "ACTIVE").setParameter(6, "PRO123")
                .executeUpdate();

        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_A_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_B_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina Professor IT")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, classroomId)
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_teachers (subject_id, member_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, teacherMemberId)
                .executeUpdate();

        // Older task: STUDENT_A evaluated (7.00), STUDENT_B submitted but pending evaluation.
        taskOlderId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,DATE_ADD(NOW(6), INTERVAL 5 DAY),?,DATE_SUB(NOW(6), INTERVAL 2 DAY),NOW(6))")
                .setParameter(1, taskOlderId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, TEACHER_ID)
                .setParameter(5, "Tarefa mais antiga").setParameter(6, "Descrição").setParameter(7, "PUBLISHED")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, grade, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskOlderId).setParameter(3, STUDENT_A_ID)
                .setParameter(4, ORG_ID).setParameter(5, "EVALUATED").setParameter(6, new BigDecimal("7.00"))
                .executeUpdate();
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskOlderId).setParameter(3, STUDENT_B_ID)
                .setParameter(4, ORG_ID).setParameter(5, "SUBMITTED")
                .executeUpdate();

        // Newer task (the "last task"): STUDENT_A evaluated (9.00), STUDENT_B never submitted.
        taskNewerId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,DATE_ADD(NOW(6), INTERVAL 10 DAY),?,NOW(6),NOW(6))")
                .setParameter(1, taskNewerId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, TEACHER_ID)
                .setParameter(5, "Tarefa mais recente").setParameter(6, "Descrição").setParameter(7, "PUBLISHED")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, grade, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskNewerId).setParameter(3, STUDENT_A_ID)
                .setParameter(4, ORG_ID).setParameter(5, "EVALUATED").setParameter(6, new BigDecimal("9.00"))
                .executeUpdate();
        tx.commit();
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_teachers WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id = ?").setParameter(1, subjectId).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?,?)")
                .setParameter(1, TEACHER_ID).setParameter(2, OTHER_TEACHER_ID).setParameter(3, STUDENT_A_ID).setParameter(4, STUDENT_B_ID)
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
    void isProfessorAssignedToSubject_teacherLinkedToSubject_returnsTrue() {
        assertThat(sut.isProfessorAssignedToSubject(subjectId, TEACHER_ID)).isTrue();
    }

    @Test
    void isProfessorAssignedToSubject_teacherNotLinkedToSubject_returnsFalse() {
        assertThat(sut.isProfessorAssignedToSubject(subjectId, OTHER_TEACHER_ID)).isFalse();
    }

    @Test
    void countPendingEvaluations_countsSubmittedAcrossAllTasksOfSubject() {
        assertThat(sut.countPendingEvaluations(subjectId)).isEqualTo(1L);
    }

    @Test
    void getLastTaskGradeDistribution_returnsEvaluatedGradesOfMostRecentTask() {
        var result = sut.getLastTaskGradeDistribution(subjectId);

        assertThat(result).containsExactly(new BigDecimal("9.00"));
    }

    @Test
    void getLastTaskGradeDistribution_lastTaskWithoutEvaluatedSubmissions_returnsEmpty() throws Exception {
        tx.begin();
        em.createNativeQuery("UPDATE task_submissions SET status = 'SUBMITTED', grade = NULL WHERE task_id = ?")
                .setParameter(1, taskNewerId).executeUpdate();
        tx.commit();

        assertThat(sut.getLastTaskGradeDistribution(subjectId)).isEmpty();
    }

    @Test
    void getLastTaskStudentsWithoutSubmission_returnsEligibleStudentsMissingFromMostRecentTask() {
        var result = sut.getLastTaskStudentsWithoutSubmission(subjectId);

        assertThat(result).extracting("studentId").containsExactly(STUDENT_B_ID);
    }

    @Test
    void getLastTaskStudentsWithoutSubmission_allStudentsSubmitted_returnsEmpty() throws Exception {
        tx.begin();
        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskNewerId).setParameter(3, STUDENT_B_ID)
                .setParameter(4, ORG_ID).setParameter(5, "SUBMITTED")
                .executeUpdate();
        tx.commit();

        assertThat(sut.getLastTaskStudentsWithoutSubmission(subjectId)).isEmpty();
    }

    @Test
    void getAverageGradePerStudent_averagesEvaluatedGradesAcrossTasks() {
        var result = sut.getAverageGradePerStudent(subjectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentId()).isEqualTo(STUDENT_A_ID);
        assertThat(result.get(0).getAverageGrade()).isEqualByComparingTo(new BigDecimal("8.00"));
    }
}
