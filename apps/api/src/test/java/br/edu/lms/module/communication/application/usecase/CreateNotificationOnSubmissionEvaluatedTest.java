package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.assessment.domain.event.SubmissionEvaluatedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNotificationOnSubmissionEvaluatedTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks CreateNotificationOnSubmissionEvaluated listener;

    private static final String ORG_ID = "org-1";
    private static final String TASK_ID = "task-1";
    private static final String SUBMISSION_ID = "submission-1";
    private static final String STUDENT_ID = "student-1";

    @Test
    void onSubmissionEvaluated_createsNotificationForStudent() {
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var event = new SubmissionEvaluatedEvent(SUBMISSION_ID, TASK_ID, STUDENT_ID, ORG_ID);
        listener.onSubmissionEvaluated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        var notification = captor.getValue();
        assertThat(notification.getUserId()).isEqualTo(STUDENT_ID);
        assertThat(notification.getType()).isEqualTo(NotificationType.SUBMISSION_EVALUATED);
        assertThat(notification.getReferenceId()).isEqualTo(TASK_ID);
        verify(notificationUnreadCounterPort, times(1)).increment(STUDENT_ID);
    }
}
