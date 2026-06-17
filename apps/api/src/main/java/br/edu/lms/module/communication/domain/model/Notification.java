package br.edu.lms.module.communication.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @EqualsAndHashCode.Include
    private final NotificationId id;

    private final String userId;
    private final String organizationId;
    private final NotificationType type;
    private final String referenceId;
    private final String title;
    private final String message;
    private final String actionLink;
    private LocalDateTime readAt;
    private final LocalDateTime createdAt;

    public boolean isRead() {
        return this.readAt != null;
    }

    public Notification markAsRead() {
        if (isRead()) {
            return this;
        }
        return this.toBuilder().readAt(LocalDateTime.now()).build();
    }
}
