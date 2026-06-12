package br.edu.lms.module.identity.domain.exception;

public class EmailAlreadyConfirmedException extends RuntimeException {
    public EmailAlreadyConfirmedException() {
        super("Email is already confirmed");
    }
}
