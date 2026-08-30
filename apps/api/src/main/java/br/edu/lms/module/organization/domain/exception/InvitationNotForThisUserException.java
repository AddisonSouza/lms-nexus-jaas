package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvitationNotForThisUserException extends RuntimeException implements HttpMappable {
    public InvitationNotForThisUserException() {
        super("Invitation was addressed to a different email");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "INVITATION_NOT_FOR_THIS_USER"; }
}
