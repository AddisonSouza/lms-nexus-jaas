package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvalidConfirmationTokenException extends RuntimeException implements HttpMappable {
    public InvalidConfirmationTokenException() {
        super("Confirmation token is invalid or expired");
    }

    @Override public int httpStatus() { return 400; }
    @Override public String errorCode() { return "INVALID_CONFIRMATION_TOKEN"; }
}
