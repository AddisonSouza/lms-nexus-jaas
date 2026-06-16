package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class DeadlineNotInFutureException extends RuntimeException implements HttpMappable {
    public DeadlineNotInFutureException() {
        super("Task deadline must be in the future");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "DEADLINE_NOT_IN_FUTURE"; }
}
