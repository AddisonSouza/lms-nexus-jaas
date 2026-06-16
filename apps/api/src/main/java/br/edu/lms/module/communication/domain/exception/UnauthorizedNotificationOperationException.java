package br.edu.lms.module.communication.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class UnauthorizedNotificationOperationException extends RuntimeException implements HttpMappable {
    public UnauthorizedNotificationOperationException(String userId, String notificationId) {
        super("User " + userId + " is not authorized to modify notification " + notificationId);
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "NOTIFICATION_FORBIDDEN"; }
}
