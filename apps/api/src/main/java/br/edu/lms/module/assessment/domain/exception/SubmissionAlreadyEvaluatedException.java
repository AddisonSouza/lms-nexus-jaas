package br.edu.lms.module.assessment.domain.exception;

public class SubmissionAlreadyEvaluatedException extends RuntimeException {
    public SubmissionAlreadyEvaluatedException(String submissionId) {
        super("Cannot edit submission already evaluated: " + submissionId);
    }
}
