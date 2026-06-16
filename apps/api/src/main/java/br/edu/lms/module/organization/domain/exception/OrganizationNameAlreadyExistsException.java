package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class OrganizationNameAlreadyExistsException extends RuntimeException implements HttpMappable {
    public OrganizationNameAlreadyExistsException() {
        super("Organization name already exists for this user");
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "ORGANIZATION_NAME_ALREADY_EXISTS"; }
}
