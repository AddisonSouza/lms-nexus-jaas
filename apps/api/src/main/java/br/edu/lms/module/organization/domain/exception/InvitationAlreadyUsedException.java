package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvitationAlreadyUsedException extends RuntimeException implements HttpMappable {
    public InvitationAlreadyUsedException() {
        super("Invitation has already been used");
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "INVITATION_ALREADY_USED"; }
}
