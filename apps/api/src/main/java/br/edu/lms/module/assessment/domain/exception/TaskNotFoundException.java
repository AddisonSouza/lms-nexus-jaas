package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class TaskNotFoundException extends RuntimeException implements HttpMappable {
    public TaskNotFoundException(String taskId) {
        super("Task not found: " + taskId);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "TASK_NOT_FOUND"; }
}
