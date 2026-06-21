package br.edu.lms.module.communication.application.dto;

import br.edu.lms.module.communication.domain.model.Notification;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NotificationListResult {
    private List<Notification> notifications;
    private long unreadCount;
}
