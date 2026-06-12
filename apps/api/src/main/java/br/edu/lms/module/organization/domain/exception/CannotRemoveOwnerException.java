package br.edu.lms.module.organization.domain.exception;

public class CannotRemoveOwnerException extends RuntimeException {
    public CannotRemoveOwnerException() {
        super("Cannot remove the organization owner");
    }
}
