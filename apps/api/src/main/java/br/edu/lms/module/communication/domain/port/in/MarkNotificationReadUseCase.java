package br.edu.lms.module.communication.domain.port.in;

import br.edu.lms.module.communication.application.dto.MarkNotificationReadResult;

public interface MarkNotificationReadUseCase {
    MarkNotificationReadResult execute(String notificationId, String userId, String organizationId);
}
