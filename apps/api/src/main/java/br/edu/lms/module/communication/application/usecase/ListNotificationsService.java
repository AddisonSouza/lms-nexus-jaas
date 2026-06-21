package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.NotificationListResult;
import br.edu.lms.module.communication.domain.port.in.ListNotificationsUseCase;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ListNotificationsService implements ListNotificationsUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    @Override
    public NotificationListResult execute(String userId, String organizationId) {
        var notifications = notificationRepository.findByUser(userId, organizationId);
        var unreadCount = notificationUnreadCounterPort.get(userId);
        return NotificationListResult.builder()
                .notifications(notifications)
                .unreadCount(unreadCount)
                .build();
    }
}
