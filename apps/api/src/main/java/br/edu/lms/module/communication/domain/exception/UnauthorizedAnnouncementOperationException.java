package br.edu.lms.module.communication.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class UnauthorizedAnnouncementOperationException extends RuntimeException implements HttpMappable {
    public UnauthorizedAnnouncementOperationException(String userId, String announcementId) {
        super("User " + userId + " is not authorized to modify announcement " + announcementId);
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "ANNOUNCEMENT_FORBIDDEN"; }
}
