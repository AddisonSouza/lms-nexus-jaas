package br.edu.lms.module.assessment.domain.exception;

public class SubmissionAlreadyExistsException extends RuntimeException {
    public SubmissionAlreadyExistsException(String taskId, String studentId) {
        super("Submission already exists for task " + taskId + " by student " + studentId);
    }
}
