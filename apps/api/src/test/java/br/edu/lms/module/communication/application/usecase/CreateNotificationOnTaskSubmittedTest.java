package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.assessment.domain.event.TaskSubmittedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import br.edu.lms.module.communication.domain.port.out.SubjectQueryPort;
import br.edu.lms.module.communication.domain.port.out.TaskQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNotificationOnTaskSubmittedTest {

    @Mock TaskQueryPort taskQueryPort;
    @Mock SubjectQueryPort subjectQueryPort;
    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks CreateNotificationOnTaskSubmitted listener;

    private static final String ORG_ID = "org-1";
    private static final String SUBJECT_ID = "subject-1";
    private static final String TASK_ID = "task-1";
    private static final String SUBMISSION_ID = "submission-1";
    private static final String STUDENT_ID = "student-1";
    private static final String TEACHER_1 = "teacher-1";
    private static final String TEACHER_2 = "teacher-2";

    @Test
    void onTaskSubmitted_createsNotificationForEachTeacher() {
        when(taskQueryPort.findSubjectIdByTask(TASK_ID)).thenReturn(Optional.of(SUBJECT_ID));
        when(subjectQueryPort.findTeacherUserIdsBySubject(SUBJECT_ID)).thenReturn(List.of(TEACHER_1, TEACHER_2));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var event = new TaskSubmittedEvent(SUBMISSION_ID, TASK_ID, STUDENT_ID, ORG_ID);
        listener.onTaskSubmitted(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notification::getUserId)
                .containsExactlyInAnyOrder(TEACHER_1, TEACHER_2);
        assertThat(captor.getAllValues())
                .allSatisfy(n -> {
                    assertThat(n.getType()).isEqualTo(NotificationType.TASK_SUBMITTED);
                    assertThat(n.getReferenceId()).isEqualTo(TASK_ID);
                });
        verify(notificationUnreadCounterPort, times(1)).increment(TEACHER_1);
        verify(notificationUnreadCounterPort, times(1)).increment(TEACHER_2);
    }

    @Test
    void onTaskSubmitted_noTeachersLinked_createsNothing() {
        when(taskQueryPort.findSubjectIdByTask(TASK_ID)).thenReturn(Optional.of(SUBJECT_ID));
        when(subjectQueryPort.findTeacherUserIdsBySubject(SUBJECT_ID)).thenReturn(List.of());

        var event = new TaskSubmittedEvent(SUBMISSION_ID, TASK_ID, STUDENT_ID, ORG_ID);
        listener.onTaskSubmitted(event);

        verify(notificationRepository, never()).save(any());
        verify(notificationUnreadCounterPort, never()).increment(any());
    }

    @Test
    void onTaskSubmitted_subjectNotFound_createsNothingAndDoesNotThrow() {
        when(taskQueryPort.findSubjectIdByTask(TASK_ID)).thenReturn(Optional.empty());

        var event = new TaskSubmittedEvent(SUBMISSION_ID, TASK_ID, STUDENT_ID, ORG_ID);
        listener.onTaskSubmitted(event);

        verify(notificationRepository, never()).save(any());
        verify(notificationUnreadCounterPort, never()).increment(any());
    }
}
