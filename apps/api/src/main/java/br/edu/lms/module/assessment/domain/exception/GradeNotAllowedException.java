package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class GradeNotAllowedException extends RuntimeException implements HttpMappable {
    public GradeNotAllowedException(String taskId) {
        super("Task " + taskId + " does not have a maximum score; grade cannot be assigned");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "GRADE_NOT_ALLOWED"; }
}
