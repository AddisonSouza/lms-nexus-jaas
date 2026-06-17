package br.edu.lms.module.communication.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class NotificationNotFoundException extends RuntimeException implements HttpMappable {
    public NotificationNotFoundException(String notificationId) {
        super("Notification not found: " + notificationId);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "NOTIFICATION_NOT_FOUND"; }
}
