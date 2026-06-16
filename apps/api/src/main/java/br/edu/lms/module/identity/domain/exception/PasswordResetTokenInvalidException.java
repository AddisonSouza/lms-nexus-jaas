package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class PasswordResetTokenInvalidException extends RuntimeException implements HttpMappable {
    public PasswordResetTokenInvalidException() {
        super("Password reset token is invalid, expired, or already used");
    }

    @Override public int httpStatus() { return 400; }
    @Override public String errorCode() { return "Token inválido, expirado ou já utilizado"; }
}
