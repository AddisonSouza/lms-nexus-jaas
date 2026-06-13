package br.edu.lms.module.classroom.domain.exception;

public class InvalidInviteCodeException extends RuntimeException {
    public InvalidInviteCodeException() {
        super("INVALID_INVITE_CODE");
    }
}
