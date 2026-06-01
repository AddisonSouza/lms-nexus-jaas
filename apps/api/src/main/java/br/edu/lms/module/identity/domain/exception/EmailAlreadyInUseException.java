package br.edu.lms.module.identity.domain.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("E-mail já em uso: " + email);
    }
}
