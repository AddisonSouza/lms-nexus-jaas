package br.edu.lms.module.identity.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class UserNotMemberOfOrganizationException extends RuntimeException implements HttpMappable {
    public UserNotMemberOfOrganizationException() {
        super("User is not a member of this organization");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "NOT_AN_ORGANIZATION_MEMBER"; }
}
