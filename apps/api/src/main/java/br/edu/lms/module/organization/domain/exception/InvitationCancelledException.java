package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvitationCancelledException extends RuntimeException implements HttpMappable {
    public InvitationCancelledException() {
        super("Invitation has been cancelled");
    }

    @Override public int httpStatus() { return 410; }
    @Override public String errorCode() { return "INVITATION_CANCELLED"; }
}
