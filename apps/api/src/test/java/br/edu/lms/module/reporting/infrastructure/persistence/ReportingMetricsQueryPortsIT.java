package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.ActivityType;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ReportingMetricsQueryPortsIT {

    static final String ORG_ID = "20000000-2000-2000-2000-200000000001";
    static final String ADMIN_ID = "20000000-2000-2000-2000-200000000002";
    static final String STUDENT_ID = "20000000-2000-2000-2000-200000000003";
    static final String TEACHER_ID = "20000000-2000-2000-2000-200000000004";

    @Inject EntityManager em;
    @Inject UserTransaction tx;
    @Inject ClassroomMetricsQueryPortImpl classroomMetricsQueryPort;
    @Inject MemberMetricsQueryPortImpl memberMetricsQueryPort;
    @Inject TaskMetricsQueryPortImpl taskMetricsQueryPort;

    String classroomId;
    String subjectId;
    String taskId;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(ADMIN_ID, "Admin IT");
        insertUser(STUDENT_ID, "Student IT");
        insertUser(TEACHER_ID, "Teacher IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID).setParameter(2, "Reporting Test Org").setParameter(3, ADMIN_ID)
                .executeUpdate();

        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, ORG_ID).setParameter(3, ADMIN_ID).setParameter(4, "ADMIN_ORG")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, ORG_ID).setParameter(3, STUDENT_ID).setParameter(4, "ALUNO")
                .executeUpdate();

        classroomId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO classrooms (id, organization_id, name, academic_period, status, invite_code, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, classroomId).setParameter(2, ORG_ID).setParameter(3, "Turma IT")
                .setParameter(4, "2026.1").setParameter(5, "ACTIVE").setParameter(6, "ABC123")
                .executeUpdate();

        em.createNativeQuery("INSERT INTO classroom_members (id, classroom_id, user_id, organization_id, role, joined_at) VALUES (?,?,?,?,?,NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, classroomId).setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID).setParameter(5, "ALUNO")
                .executeUpdate();

        subjectId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, subjectId).setParameter(2, ORG_ID).setParameter(3, "Disciplina IT")
                .executeUpdate();
        em.createNativeQuery("INSERT INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, subjectId).setParameter(2, classroomId)
                .executeUpdate();

        taskId = UUID.randomUUID().toString();
        em.createNativeQuery("INSERT INTO tasks (id, subject_id, organization_id, created_by, title, description, deadline, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,?,NOW(6),?,NOW(6),NOW(6))")
                .setParameter(1, taskId).setParameter(2, subjectId).setParameter(3, ORG_ID).setParameter(4, TEACHER_ID)
                .setParameter(5, "Tarefa IT").setParameter(6, "Descrição").setParameter(7, "PUBLISHED")
                .executeUpdate();

        em.createNativeQuery("INSERT INTO task_submissions (id, task_id, student_id, organization_id, status, created_at, updated_at) " +
                        "VALUES (?,?,?,?,?,NOW(6),NOW(6))")
                .setParameter(1, UUID.randomUUID().toString()).setParameter(2, taskId).setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID).setParameter(5, "EVALUATED")
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
        em.createNativeQuery("DELETE FROM organization_members WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?,?)")
                .setParameter(1, ADMIN_ID).setParameter(2, STUDENT_ID).setParameter(3, TEACHER_ID)
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
    void countByStatus_returnsActiveClassroomCount() {
        var result = classroomMetricsQueryPort.countByStatus(ORG_ID);

        assertThat(result).containsEntry("ACTIVE", 1L);
    }

    @Test
    void countByRole_returnsMembersGroupedByRole() {
        var result = memberMetricsQueryPort.countByRole(ORG_ID);

        assertThat(result).containsEntry("ADMIN_ORG", 1L).containsEntry("ALUNO", 1L);
    }

    @Test
    void taskMetrics_countCreatedAndEvaluated_andAverageDeliveryRate() {
        var period = new DashboardPeriod(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertThat(taskMetricsQueryPort.countCreated(ORG_ID, period)).isEqualTo(1L);
        assertThat(taskMetricsQueryPort.countEvaluated(ORG_ID, period)).isEqualTo(1L);
        assertThat(taskMetricsQueryPort.averageDeliveryRate(ORG_ID, period))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void taskMetrics_listActivity_includesCreatedAndEvaluated() {
        var period = new DashboardPeriod(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        var activity = taskMetricsQueryPort.listActivity(ORG_ID, period);

        assertThat(activity).extracting("type").contains(ActivityType.TASK_CREATED, ActivityType.TASK_EVALUATED);
    }

    @Test
    void taskMetrics_periodOutsideRange_returnsZeroedMetrics() {
        var period = new DashboardPeriod(LocalDate.now().minusDays(60), LocalDate.now().minusDays(30));

        assertThat(taskMetricsQueryPort.countCreated(ORG_ID, period)).isZero();
        assertThat(taskMetricsQueryPort.averageDeliveryRate(ORG_ID, period)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
