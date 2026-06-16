package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class EmailAlreadyInUseException extends RuntimeException implements HttpMappable {

    public EmailAlreadyInUseException(String email) {
        super("E-mail já em uso: " + email);
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return getMessage(); }
}
