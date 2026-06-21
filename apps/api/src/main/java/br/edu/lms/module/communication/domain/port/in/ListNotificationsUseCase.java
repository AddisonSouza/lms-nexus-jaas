package br.edu.lms.module.communication.domain.port.in;

import br.edu.lms.module.communication.application.dto.NotificationListResult;

public interface ListNotificationsUseCase {
    NotificationListResult execute(String userId, String organizationId);
}
