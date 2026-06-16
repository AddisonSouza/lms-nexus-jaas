package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class TokenNotFoundException extends RuntimeException implements HttpMappable {
    public TokenNotFoundException() {
        super("Refresh token not found or expired");
    }

    @Override public int httpStatus() { return 401; }
    @Override public String errorCode() { return "Unauthorized"; }
}
