package br.edu.lms.module.communication.domain.port.out;

import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(NotificationId id, String organizationId);
    List<Notification> findByUser(String userId, String organizationId);
    long countUnreadByUser(String userId, String organizationId);
    Notification markRead(NotificationId id);
    void markAllReadByUser(String userId, String organizationId);
}
