package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class DeadlineExpiredException extends RuntimeException implements HttpMappable {
    public DeadlineExpiredException(String taskId) {
        super("Task deadline has expired: " + taskId);
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "DEADLINE_EXPIRED"; }
}
