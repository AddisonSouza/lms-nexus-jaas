package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.event.AnnouncementPostedEvent;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
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
class CreateNotificationOnAnnouncementPostedTest {

    @Mock ClassroomQueryPort classroomQueryPort;
    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks CreateNotificationOnAnnouncementPosted listener;

    private static final String ORG_ID = "org-1";
    private static final String CLASSROOM_ID = "classroom-1";
    private static final String AUTHOR_ID = "prof-1";
    private static final String STUDENT_ID = "student-1";

    @Test
    void onAnnouncementPosted_createsNotificationForEachStudent_excludingAuthor() {
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_ID, "ALUNO"))
                .thenReturn(List.of(STUDENT_ID, AUTHOR_ID));
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var event = new AnnouncementPostedEvent("ann-1", CLASSROOM_ID, AUTHOR_ID, ORG_ID);
        listener.onAnnouncementPosted(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        var notification = captor.getValue();
        assertThat(notification.getUserId()).isEqualTo(STUDENT_ID);
        assertThat(notification.getType()).isEqualTo(NotificationType.ANNOUNCEMENT_POSTED);
        assertThat(notification.getReferenceId()).isEqualTo("ann-1");
        assertThat(notification.getActionLink()).isEqualTo("/classrooms/" + CLASSROOM_ID);
        verify(notificationUnreadCounterPort, times(1)).increment(STUDENT_ID);
        verify(notificationUnreadCounterPort, never()).increment(AUTHOR_ID);
    }

    @Test
    void onAnnouncementPosted_noRecipients_createsNothing() {
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_ID, "ALUNO")).thenReturn(List.of());

        var event = new AnnouncementPostedEvent("ann-1", CLASSROOM_ID, AUTHOR_ID, ORG_ID);
        listener.onAnnouncementPosted(event);

        verify(notificationRepository, never()).save(any());
        verify(notificationUnreadCounterPort, never()).increment(any());
    }

    @Test
    void onAnnouncementPosted_onlyAuthorIsMember_createsNothing() {
        when(classroomQueryPort.listMemberUserIds(CLASSROOM_ID, "ALUNO")).thenReturn(List.of(AUTHOR_ID));

        var event = new AnnouncementPostedEvent("ann-1", CLASSROOM_ID, AUTHOR_ID, ORG_ID);
        listener.onAnnouncementPosted(event);

        verify(notificationRepository, never()).save(any());
        verify(notificationUnreadCounterPort, never()).increment(any());
    }
}
