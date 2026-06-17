package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.port.in.MarkAllNotificationsReadUseCase;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class MarkAllNotificationsReadService implements MarkAllNotificationsReadUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationUnreadCounterPort notificationUnreadCounterPort;

    @Override
    public void execute(String userId, String organizationId) {
        notificationRepository.markAllReadByUser(userId, organizationId);
        notificationUnreadCounterPort.reset(userId);
    }
}
