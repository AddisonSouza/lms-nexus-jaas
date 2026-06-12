package br.edu.lms.module.organization.domain.exception;

public class OrganizationNameAlreadyExistsException extends RuntimeException {
    public OrganizationNameAlreadyExistsException() {
        super("Organization name already exists for this user");
    }
}
