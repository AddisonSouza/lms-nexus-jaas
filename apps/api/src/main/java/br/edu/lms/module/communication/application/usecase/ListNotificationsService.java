package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.port.in.ListNotificationsUseCase;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListNotificationsService implements ListNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public List<Notification> execute(String userId, String organizationId) {
        return notificationRepository.findByUser(userId, organizationId);
    }
}
