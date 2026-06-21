package br.edu.lms.module.communication.application.usecase;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListNotificationsServiceTest {

    @Mock NotificationRepository notificationRepository;
    @Mock NotificationUnreadCounterPort notificationUnreadCounterPort;
    @InjectMocks ListNotificationsService service;

    private static final String ORG_ID = "org-1";
    private static final String USER_ID = "user-1";

    @Test
    void execute_returnsNotificationsFromRepository() {
        var notification = Notification.builder()
                .id(NotificationId.generate())
                .userId(USER_ID)
                .organizationId(ORG_ID)
                .type(NotificationType.ANNOUNCEMENT_POSTED)
                .referenceId("ann-1")
                .title("Novo aviso")
                .message("Um novo aviso foi publicado na turma.")
                .actionLink("/classrooms/classroom-1")
                .createdAt(LocalDateTime.now())
                .build();
        when(notificationRepository.findByUser(USER_ID, ORG_ID)).thenReturn(List.of(notification));
        when(notificationUnreadCounterPort.get(USER_ID)).thenReturn(1L);

        var result = service.execute(USER_ID, ORG_ID);

        assertThat(result.getNotifications()).hasSize(1);
        assertThat(result.getNotifications().get(0).getTitle()).isEqualTo("Novo aviso");
        assertThat(result.getUnreadCount()).isEqualTo(1L);
    }

    @Test
    void execute_noNotifications_returnsEmptyList() {
        when(notificationRepository.findByUser(USER_ID, ORG_ID)).thenReturn(List.of());
        when(notificationUnreadCounterPort.get(USER_ID)).thenReturn(0L);

        var result = service.execute(USER_ID, ORG_ID);

        assertThat(result.getNotifications()).isEmpty();
        assertThat(result.getUnreadCount()).isZero();
    }
}
