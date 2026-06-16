package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class AlreadyAMemberException extends RuntimeException implements HttpMappable {
    public AlreadyAMemberException() {
        super("User is already a member of this organization");
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "ALREADY_A_MEMBER"; }
}
