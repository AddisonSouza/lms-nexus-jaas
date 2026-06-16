package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class EmailAlreadyConfirmedException extends RuntimeException implements HttpMappable {
    public EmailAlreadyConfirmedException() {
        super("Email is already confirmed");
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "EMAIL_ALREADY_CONFIRMED"; }
}
