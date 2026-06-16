package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class UnauthorizedTaskOperationException extends RuntimeException implements HttpMappable {
    public UnauthorizedTaskOperationException(String userId, String taskId) {
        super("User " + userId + " is not authorized to modify task " + taskId);
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "TASK_FORBIDDEN"; }
}
