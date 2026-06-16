package br.edu.lms.module.classroom.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvalidInviteCodeException extends RuntimeException implements HttpMappable {
    public InvalidInviteCodeException() {
        super("INVALID_INVITE_CODE");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "INVALID_INVITE_CODE"; }
}
