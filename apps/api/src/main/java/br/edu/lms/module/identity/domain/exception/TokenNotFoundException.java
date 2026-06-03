package br.edu.lms.module.identity.domain.exception;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException() {
        super("Refresh token not found or expired");
    }
}
