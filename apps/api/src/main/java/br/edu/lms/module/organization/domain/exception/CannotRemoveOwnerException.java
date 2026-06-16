package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class CannotRemoveOwnerException extends RuntimeException implements HttpMappable {
    public CannotRemoveOwnerException() {
        super("Cannot remove the organization owner");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "CANNOT_REMOVE_OWNER"; }
}
