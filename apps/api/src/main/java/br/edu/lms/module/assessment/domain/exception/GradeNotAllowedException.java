package br.edu.lms.module.assessment.domain.exception;

public class GradeNotAllowedException extends RuntimeException {
    public GradeNotAllowedException(String taskId) {
        super("Task " + taskId + " does not have a maximum score; grade cannot be assigned");
    }
}
