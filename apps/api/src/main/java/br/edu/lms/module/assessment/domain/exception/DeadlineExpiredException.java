package br.edu.lms.module.assessment.domain.exception;

public class DeadlineExpiredException extends RuntimeException {
    public DeadlineExpiredException(String taskId) {
        super("Task deadline has expired: " + taskId);
    }
}
