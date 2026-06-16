package br.edu.lms.module.assessment.domain.exception;

public class UnauthorizedTaskOperationException extends RuntimeException {
    public UnauthorizedTaskOperationException(String userId, String taskId) {
        super("User " + userId + " is not authorized to modify task " + taskId);
    }
}
