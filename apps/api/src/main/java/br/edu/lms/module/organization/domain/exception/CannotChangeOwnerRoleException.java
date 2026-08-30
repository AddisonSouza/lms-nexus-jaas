package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class CannotChangeOwnerRoleException extends RuntimeException implements HttpMappable {
    public CannotChangeOwnerRoleException() {
        super("Cannot change the role of the organization owner");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "CANNOT_CHANGE_OWNER_ROLE"; }
}
