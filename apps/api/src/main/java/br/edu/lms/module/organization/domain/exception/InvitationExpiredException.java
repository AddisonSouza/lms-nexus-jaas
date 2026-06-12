package br.edu.lms.module.organization.domain.exception;

public class InvitationExpiredException extends RuntimeException {
    public InvitationExpiredException() {
        super("Invitation has expired");
    }
}
