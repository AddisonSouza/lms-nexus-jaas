package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvitationNotFoundException extends RuntimeException implements HttpMappable {
    public InvitationNotFoundException() {
        super("Invitation not found");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "INVITATION_NOT_FOUND"; }
}
