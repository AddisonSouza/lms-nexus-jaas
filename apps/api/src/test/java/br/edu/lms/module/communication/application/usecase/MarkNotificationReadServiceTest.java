package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.exception.NotificationNotFoundException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedNotificationOperationException;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.model.NotificationType;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNotificationReadServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks MarkNotificationReadService service;

    private static final String ORG_ID = "org-1";
    private static final String USER_ID = "user-1";
    private static final String OTHER_USER_ID = "user-2";
    private static final String NOTIFICATION_ID = "notif-1";

    private Notification unreadNotification() {
        return Notification.builder()
                .id(NotificationId.of(NOTIFICATION_ID))
                .userId(USER_ID)
                .organizationId(ORG_ID)
                .type(NotificationType.ANNOUNCEMENT_POSTED)
                .referenceId("ann-1")
                .title("Novo aviso")
                .message("Um novo aviso foi publicado na turma.")
                .actionLink("/classrooms/classroom-1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void execute_marksAsReadAndDecrementsCounter() {
        var notification = unreadNotification();
        when(notificationRepository.findById(NotificationId.of(NOTIFICATION_ID), ORG_ID))
                .thenReturn(Optional.of(notification));
        var read = notification.markAsRead();
        when(notificationRepository.markRead(notification.getId())).thenReturn(read);

        var result = service.execute(NOTIFICATION_ID, USER_ID, ORG_ID);

        assertThat(result.getNotification().isRead()).isTrue();
        verify(notificationUnreadCounterPort, times(1)).decrement(USER_ID);
    }

    @Test
    void execute_notificationNotFound_throws404() {
        when(notificationRepository.findById(NotificationId.of(NOTIFICATION_ID), ORG_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(NOTIFICATION_ID, USER_ID, ORG_ID))
                .isInstanceOf(NotificationNotFoundException.class);

        verify(notificationRepository, never()).markRead(any());
        verify(notificationUnreadCounterPort, never()).decrement(any());
    }

    @Test
    void execute_notificationBelongsToAnotherUser_throws403() {
        var notification = unreadNotification();
        when(notificationRepository.findById(NotificationId.of(NOTIFICATION_ID), ORG_ID))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> service.execute(NOTIFICATION_ID, OTHER_USER_ID, ORG_ID))
                .isInstanceOf(UnauthorizedNotificationOperationException.class);

        verify(notificationRepository, never()).markRead(any());
        verify(notificationUnreadCounterPort, never()).decrement(any());
    }

    @Test
    void execute_alreadyRead_isIdempotent_doesNotDecrementAgain() {
        var notification = unreadNotification().markAsRead();
        when(notificationRepository.findById(NotificationId.of(NOTIFICATION_ID), ORG_ID))
                .thenReturn(Optional.of(notification));

        var result = service.execute(NOTIFICATION_ID, USER_ID, ORG_ID);

        assertThat(result.getNotification().isRead()).isTrue();
        verify(notificationRepository, never()).markRead(any());
        verify(notificationUnreadCounterPort, never()).decrement(any());
    }
}
