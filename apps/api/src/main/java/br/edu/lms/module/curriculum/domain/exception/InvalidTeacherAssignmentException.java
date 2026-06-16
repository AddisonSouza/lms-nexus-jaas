package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvalidTeacherAssignmentException extends RuntimeException implements HttpMappable {
    public InvalidTeacherAssignmentException(String reason) {
        super(reason);
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return getMessage(); }
}
