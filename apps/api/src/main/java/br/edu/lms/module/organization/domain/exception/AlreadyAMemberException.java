package br.edu.lms.module.organization.domain.exception;

public class AlreadyAMemberException extends RuntimeException {
    public AlreadyAMemberException() {
        super("User is already a member of this organization");
    }
}
