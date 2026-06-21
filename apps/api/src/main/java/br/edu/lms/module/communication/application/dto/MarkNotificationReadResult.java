package br.edu.lms.module.communication.application.dto;

import br.edu.lms.module.communication.domain.model.Notification;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarkNotificationReadResult {
    private Notification notification;
    private long unreadCount;
}
