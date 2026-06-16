package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvalidCredentialsException extends RuntimeException implements HttpMappable {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

    @Override public int httpStatus() { return 401; }
    @Override public String errorCode() { return "Unauthorized"; }
}
