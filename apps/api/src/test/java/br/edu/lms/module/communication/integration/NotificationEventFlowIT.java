package br.edu.lms.module.communication.integration;

import br.edu.lms.module.assessment.application.dto.CreateTaskCommand;
import br.edu.lms.module.assessment.application.dto.EvaluateSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmitTaskCommand;
import br.edu.lms.module.assessment.domain.port.in.CreateTaskUseCase;
import br.edu.lms.module.assessment.domain.port.in.EvaluateSubmissionUseCase;
import br.edu.lms.module.assessment.domain.port.in.PublishTaskUseCase;
import br.edu.lms.module.assessment.domain.port.in.SubmitTaskUseCase;
import br.edu.lms.module.communication.application.dto.PostAnnouncementCommand;
import br.edu.lms.module.communication.domain.port.in.PostAnnouncementUseCase;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test: invokes the *originating* use cases from communication (RF-15) and
 * assessment (RF-13/RF-14) modules to fire the real Domain Events, and verifies the
 * communication module's listeners persist Notifications and increment the Redis
 * unread counter — without mocking any port.
 */
@QuarkusTest
class NotificationEventFlowIT {

    static final String ORG_ID = "20000000-2000-2000-2000-200000000001";
    static final String PROFESSOR_ID = "20000000-2000-2000-2000-200000000002";
    static final String STUDENT_ID = "20000000-2000-2000-2000-200000000003";
    static final String CLASSROOM_ID = "20000000-2000-2000-2000-200000000004";
    static final String SUBJECT_ID = "20000000-2000-2000-2000-200000000005";
    static final String ORG_MEMBER_ID = "20000000-2000-2000-2000-200000000006";

    @Inject EntityManager em;
    @Inject UserTransaction tx;

    @Inject PostAnnouncementUseCase postAnnouncementUseCase;
    @Inject CreateTaskUseCase createTaskUseCase;
    @Inject PublishTaskUseCase publishTaskUseCase;
    @Inject SubmitTaskUseCase submitTaskUseCase;
    @Inject EvaluateSubmissionUseCase evaluateSubmissionUseCase;
    @Inject NotificationUnreadCounterPort notificationUnreadCounterPort;

    @BeforeEach
    void setUp() throws Exception {
        tx.begin();
        insertUser(PROFESSOR_ID, "Professor Flow IT");
        insertUser(STUDENT_ID, "Aluno Flow IT");

        em.createNativeQuery("INSERT IGNORE INTO organizations (id, name, owner_id, created_at) VALUES (?,?,?,NOW(6))")
                .setParameter(1, ORG_ID)
                .setParameter(2, "Flow IT Org")
                .setParameter(3, PROFESSOR_ID)
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO classrooms (id, organization_id, name, academic_period, invite_code) VALUES (?,?,?,?,?)")
                .setParameter(1, CLASSROOM_ID)
                .setParameter(2, ORG_ID)
                .setParameter(3, "Turma Flow IT")
                .setParameter(4, "2026.1")
                .setParameter(5, "FLOW001")
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO classroom_members (id, classroom_id, user_id, organization_id, role) VALUES (?,?,?,?,?)")
                .setParameter(1, "cm-prof-flow")
                .setParameter(2, CLASSROOM_ID)
                .setParameter(3, PROFESSOR_ID)
                .setParameter(4, ORG_ID)
                .setParameter(5, "PROFESSOR")
                .executeUpdate();
        em.createNativeQuery("INSERT IGNORE INTO classroom_members (id, classroom_id, user_id, organization_id, role) VALUES (?,?,?,?,?)")
                .setParameter(1, "cm-student-flow")
                .setParameter(2, CLASSROOM_ID)
                .setParameter(3, STUDENT_ID)
                .setParameter(4, ORG_ID)
                .setParameter(5, "ALUNO")
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO organization_members (id, organization_id, user_id, role, joined_at) VALUES (?,?,?,?,NOW(6))")
                .setParameter(1, ORG_MEMBER_ID)
                .setParameter(2, ORG_ID)
                .setParameter(3, PROFESSOR_ID)
                .setParameter(4, "PROFESSOR")
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO subjects (id, organization_id, name, created_at, updated_at) VALUES (?,?,?,NOW(6),NOW(6))")
                .setParameter(1, SUBJECT_ID)
                .setParameter(2, ORG_ID)
                .setParameter(3, "Disciplina Flow IT")
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO subject_classrooms (subject_id, classroom_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, SUBJECT_ID)
                .setParameter(2, CLASSROOM_ID)
                .executeUpdate();

        em.createNativeQuery("INSERT IGNORE INTO subject_teachers (subject_id, member_id, created_at) VALUES (?,?,NOW(6))")
                .setParameter(1, SUBJECT_ID)
                .setParameter(2, ORG_MEMBER_ID)
                .executeUpdate();

        tx.commit();

        notificationUnreadCounterPort.reset(STUDENT_ID);
        notificationUnreadCounterPort.reset(PROFESSOR_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        tx.begin();
        em.createNativeQuery("DELETE FROM notifications WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM announcement_attachments WHERE announcement_id IN (SELECT id FROM announcements WHERE classroom_id = ?)")
                .setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM announcements WHERE classroom_id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM task_submissions WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM tasks WHERE organization_id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_teachers WHERE subject_id = ?").setParameter(1, SUBJECT_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subject_classrooms WHERE subject_id = ?").setParameter(1, SUBJECT_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM subjects WHERE id = ?").setParameter(1, SUBJECT_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organization_members WHERE id = ?").setParameter(1, ORG_MEMBER_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classroom_members WHERE classroom_id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM classrooms WHERE id = ?").setParameter(1, CLASSROOM_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM organizations WHERE id = ?").setParameter(1, ORG_ID).executeUpdate();
        em.createNativeQuery("DELETE FROM users WHERE id IN (?,?)")
                .setParameter(1, PROFESSOR_ID).setParameter(2, STUDENT_ID)
                .executeUpdate();
        tx.commit();

        notificationUnreadCounterPort.reset(STUDENT_ID);
        notificationUnreadCounterPort.reset(PROFESSOR_ID);
    }

    private void insertUser(String id, String name) {
        em.createNativeQuery("INSERT IGNORE INTO users (id, full_name, email, password_hash, status) VALUES (?,?,?,?,?)")
                .setParameter(1, id)
                .setParameter(2, name)
                .setParameter(3, id + "@test.com")
                .setParameter(4, "$2b$10$placeholder")
                .setParameter(5, "ACTIVE")
                .executeUpdate();
    }

    @Test
    void postingAnnouncement_createsNotificationForStudentAndIncrementsCounter() {
        var command = PostAnnouncementCommand.builder()
                .classroomId(CLASSROOM_ID)
                .organizationId(ORG_ID)
                .authorId(PROFESSOR_ID)
                .content("Aviso do fluxo end-to-end")
                .build();

        postAnnouncementUseCase.execute(command);

        long count = countNotifications(STUDENT_ID, "ANNOUNCEMENT_POSTED");
        assertThat(count).isEqualTo(1);
        assertThat(notificationUnreadCounterPort.get(STUDENT_ID)).isEqualTo(1);
        assertThat(notificationUnreadCounterPort.get(PROFESSOR_ID)).isEqualTo(0);
    }

    @Test
    void publishingTask_createsNotificationForStudentAndIncrementsCounter() {
        var taskId = createDraftTask();

        publishTaskUseCase.execute(taskId, ORG_ID, PROFESSOR_ID);

        long count = countNotifications(STUDENT_ID, "TASK_PUBLISHED");
        assertThat(count).isEqualTo(1);
        assertThat(notificationUnreadCounterPort.get(STUDENT_ID)).isEqualTo(1);
    }

    @Test
    void submittingTask_createsNotificationForTeacherAndIncrementsCounter() {
        var taskId = createDraftTask();
        publishTaskUseCase.execute(taskId, ORG_ID, PROFESSOR_ID);
        notificationUnreadCounterPort.reset(STUDENT_ID);

        var submitCommand = SubmitTaskCommand.builder()
                .taskId(taskId)
                .studentId(STUDENT_ID)
                .organizationId(ORG_ID)
                .textResponse("Minha resposta")
                .build();

        submitTaskUseCase.execute(submitCommand);

        long count = countNotifications(PROFESSOR_ID, "TASK_SUBMITTED");
        assertThat(count).isEqualTo(1);
        assertThat(notificationUnreadCounterPort.get(PROFESSOR_ID)).isEqualTo(1);
    }

    @Test
    void evaluatingSubmission_createsNotificationForStudentAndIncrementsCounter() {
        var taskId = createDraftTask();
        publishTaskUseCase.execute(taskId, ORG_ID, PROFESSOR_ID);

        var submitCommand = SubmitTaskCommand.builder()
                .taskId(taskId)
                .studentId(STUDENT_ID)
                .organizationId(ORG_ID)
                .textResponse("Minha resposta")
                .build();
        var submission = submitTaskUseCase.execute(submitCommand);
        notificationUnreadCounterPort.reset(STUDENT_ID);

        var evaluateCommand = EvaluateSubmissionCommand.builder()
                .submissionId(submission.getId())
                .professorId(PROFESSOR_ID)
                .organizationId(ORG_ID)
                .grade(BigDecimal.valueOf(9.5))
                .feedback("Muito bem")
                .build();

        evaluateSubmissionUseCase.execute(evaluateCommand);

        long count = countNotifications(STUDENT_ID, "SUBMISSION_EVALUATED");
        assertThat(count).isEqualTo(1);
        assertThat(notificationUnreadCounterPort.get(STUDENT_ID)).isEqualTo(1);
    }

    private String createDraftTask() {
        var command = CreateTaskCommand.builder()
                .subjectId(SUBJECT_ID)
                .organizationId(ORG_ID)
                .createdBy(PROFESSOR_ID)
                .title("Tarefa Flow IT")
                .description("Descricao da tarefa de teste")
                .deadline(LocalDateTime.now().plusDays(3))
                .maxScore(BigDecimal.TEN)
                .build();
        return createTaskUseCase.execute(command).getId();
    }

    private long countNotifications(String userId, String type) {
        return em.createQuery(
                        "SELECT COUNT(n) FROM br.edu.lms.module.communication.infrastructure.persistence.NotificationJpaEntity n " +
                                "WHERE n.userId = :uid AND n.organizationId = :orgId AND n.type = :type",
                        Long.class)
                .setParameter("uid", userId)
                .setParameter("orgId", ORG_ID)
                .setParameter("type", type)
                .getSingleResult();
    }
}
