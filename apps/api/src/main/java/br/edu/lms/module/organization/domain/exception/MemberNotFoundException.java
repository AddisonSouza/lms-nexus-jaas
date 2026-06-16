package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class MemberNotFoundException extends RuntimeException implements HttpMappable {
    public MemberNotFoundException() {
        super("Member not found in this organization");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "MEMBER_NOT_FOUND"; }
}
