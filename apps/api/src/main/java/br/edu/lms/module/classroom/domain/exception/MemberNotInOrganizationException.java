package br.edu.lms.module.classroom.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class MemberNotInOrganizationException extends RuntimeException implements HttpMappable {
    public MemberNotInOrganizationException() {
        super("MEMBER_NOT_IN_ORGANIZATION");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "MEMBER_NOT_IN_ORGANIZATION"; }
}
