package br.edu.lms.module.organization.domain.exception;

public class InvitationNotFoundException extends RuntimeException {
    public InvitationNotFoundException() {
        super("Invitation not found");
    }
}
