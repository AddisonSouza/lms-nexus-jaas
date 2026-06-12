package br.edu.lms.module.organization.domain.exception;

public class InvitationAlreadyUsedException extends RuntimeException {
    public InvitationAlreadyUsedException() {
        super("Invitation has already been used");
    }
}
