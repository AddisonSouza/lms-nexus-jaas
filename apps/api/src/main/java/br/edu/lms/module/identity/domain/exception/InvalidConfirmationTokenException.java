package br.edu.lms.module.identity.domain.exception;

public class InvalidConfirmationTokenException extends RuntimeException {
    public InvalidConfirmationTokenException() {
        super("Confirmation token is invalid or expired");
    }
}
