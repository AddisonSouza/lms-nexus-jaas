package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class NotAnOrganizationMemberException extends RuntimeException implements HttpMappable {
    public NotAnOrganizationMemberException() {
        super("User is not a member of this organization");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "NOT_AN_ORGANIZATION_MEMBER"; }
}
