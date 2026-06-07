package br.edu.lms.module.identity.domain.exception;

public class PasswordResetTokenInvalidException extends RuntimeException {
    public PasswordResetTokenInvalidException() {
        super("Password reset token is invalid, expired, or already used");
    }
}
