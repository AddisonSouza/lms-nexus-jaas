package br.edu.lms.module.organization.domain.exception;

public class NotAnOrganizationMemberException extends RuntimeException {
    public NotAnOrganizationMemberException() {
        super("User is not a member of this organization");
    }
}
