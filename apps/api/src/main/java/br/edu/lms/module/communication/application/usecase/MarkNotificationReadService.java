package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.MarkNotificationReadResult;
import br.edu.lms.module.communication.domain.exception.NotificationNotFoundException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedNotificationOperationException;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.port.in.MarkNotificationReadUseCase;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class MarkNotificationReadService implements MarkNotificationReadUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    @Override
    public MarkNotificationReadResult execute(String notificationId, String userId, String organizationId) {
        var notification = notificationRepository.findById(NotificationId.of(notificationId), organizationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedNotificationOperationException(userId, notificationId);
        }

        if (notification.isRead()) {
            return MarkNotificationReadResult.builder()
                    .notification(notification)
                    .unreadCount(notificationUnreadCounterPort.get(userId))
                    .build();
        }

        var updated = notificationRepository.markRead(notification.getId());
        notificationUnreadCounterPort.decrement(userId);

        return MarkNotificationReadResult.builder()
                .notification(updated)
                .unreadCount(notificationUnreadCounterPort.get(userId))
                .build();
    }
}
