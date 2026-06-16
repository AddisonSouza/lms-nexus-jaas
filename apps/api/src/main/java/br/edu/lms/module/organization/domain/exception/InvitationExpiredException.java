package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvitationExpiredException extends RuntimeException implements HttpMappable {
    public InvitationExpiredException() {
        super("Invitation has expired");
    }

    @Override public int httpStatus() { return 410; }
    @Override public String errorCode() { return "INVITATION_EXPIRED"; }
}
