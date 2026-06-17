package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.assessment.domain.event.TaskPublishedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import br.edu.lms.module.communication.domain.port.out.SubjectQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNotificationOnTaskPublishedTest {

    @Mock SubjectQueryPort subjectQueryPort;
    @Mock ClassroomQueryPort classroomQueryPort;
    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks CreateNotificationOnTaskPublished listener;

    private static final String ORG_ID = "org-1";
    private static final String SUBJECT_ID = "subject-1";
    private static final String TASK_ID = "task-1";
    private static final String CLASSROOM_A = "classroom-a";
    private static final String CLASSROOM_B = "classroom-b";
    private static final String STUDENT_1 = "student-1";
    private static final String STUDENT_2 = "student-2";

    @Test
    void onTaskPublished_createsNotificationForEachStudentInEachClassroom() {
        when(subjectQueryPort.findClassroomIdsBySubject(SUBJECT_ID)).thenReturn(List.of(CLASSROOM_A, CLASSROOM_B));
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_A, "ALUNO")).thenReturn(List.of(STUDENT_1));
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_B, "ALUNO")).thenReturn(List.of(STUDENT_2));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var event = new TaskPublishedEvent(TASK_ID, SUBJECT_ID, ORG_ID);
        listener.onTaskPublished(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notification::getUserId)
                .containsExactlyInAnyOrder(STUDENT_1, STUDENT_2);
        assertThat(captor.getAllValues())
                .allSatisfy(n -> {
                    assertThat(n.getType()).isEqualTo(NotificationType.TASK_PUBLISHED);
                    assertThat(n.getReferenceId()).isEqualTo(TASK_ID);
                });
        verify(notificationUnreadCounterPort, times(1)).increment(STUDENT_1);
        verify(notificationUnreadCounterPort, times(1)).increment(STUDENT_2);
    }

    @Test
    void onTaskPublished_deduplicatesStudentAcrossClassrooms() {
        when(subjectQueryPort.findClassroomIdsBySubject(SUBJECT_ID)).thenReturn(List.of(CLASSROOM_A, CLASSROOM_B));
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_A, "ALUNO")).thenReturn(List.of(STUDENT_1));
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_B, "ALUNO")).thenReturn(List.of(STUDENT_1));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var event = new TaskPublishedEvent(TASK_ID, SUBJECT_ID, ORG_ID);
        listener.onTaskPublished(event);

        verify(notificationRepository, times(1)).save(any());
        verify(notificationUnreadCounterPort, times(1)).increment(STUDENT_1);
    }

    @Test
    void onTaskPublished_noClassroomsLinked_createsNothing() {
        when(subjectQueryPort.findClassroomIdsBySubject(SUBJECT_ID)).thenReturn(List.of());

        var event = new TaskPublishedEvent(TASK_ID, SUBJECT_ID, ORG_ID);
        listener.onTaskPublished(event);

        verify(notificationRepository, never()).save(any());
        verify(notificationUnreadCounterPort, never()).increment(any());
    }

    @Test
    void onTaskPublished_classroomWithoutStudents_createsNothing() {
        when(subjectQueryPort.findClassroomIdsBySubject(SUBJECT_ID)).thenReturn(List.of(CLASSROOM_A));
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_A, "ALUNO")).thenReturn(List.of());

        var event = new TaskPublishedEvent(TASK_ID, SUBJECT_ID, ORG_ID);
        listener.onTaskPublished(event);

        verify(notificationRepository, never()).save(any());
        verify(notificationUnreadCounterPort, never()).increment(any());
    }
}
