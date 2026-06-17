package br.edu.lms.module.communication.domain.port.in;

import br.edu.lms.module.communication.domain.model.Notification;

public interface MarkNotificationReadUseCase {
    Notification execute(String notificationId, String userId, String organizationId);
}
