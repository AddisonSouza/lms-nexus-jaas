package br.edu.lms.module.classroom.domain.exception;

public class MemberNotInOrganizationException extends RuntimeException {
    public MemberNotInOrganizationException() {
        super("MEMBER_NOT_IN_ORGANIZATION");
    }
}
