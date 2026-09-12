package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvitationNotPendingException extends RuntimeException implements HttpMappable {
    public InvitationNotPendingException() {
        super("Only a pending invitation can be cancelled");
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "INVITATION_NOT_PENDING"; }
}
