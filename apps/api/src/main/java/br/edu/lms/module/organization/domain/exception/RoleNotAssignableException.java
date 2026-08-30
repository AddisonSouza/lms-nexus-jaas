package br.edu.lms.module.organization.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

/** ADMIN_ORG pertence a quem criou a organização — não se atribui a um membro. */
public class RoleNotAssignableException extends RuntimeException implements HttpMappable {
    public RoleNotAssignableException() {
        super("This role cannot be assigned to a member");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "ROLE_NOT_ASSIGNABLE"; }
}
