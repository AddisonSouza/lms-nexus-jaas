package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubmissionAlreadyEvaluatedException extends RuntimeException implements HttpMappable {
    public SubmissionAlreadyEvaluatedException(String submissionId) {
        super("Cannot edit submission already evaluated: " + submissionId);
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "SUBMISSION_ALREADY_EVALUATED"; }
}
