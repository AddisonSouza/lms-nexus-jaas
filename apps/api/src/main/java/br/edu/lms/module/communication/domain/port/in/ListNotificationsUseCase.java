package br.edu.lms.module.communication.domain.port.in;

import br.edu.lms.module.communication.domain.model.Notification;

import java.util.List;

public interface ListNotificationsUseCase {
    List<Notification> execute(String userId, String organizationId);
}
