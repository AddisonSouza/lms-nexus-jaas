package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubmissionNotFoundException extends RuntimeException implements HttpMappable {
    public SubmissionNotFoundException(String submissionId) {
        super("Submission not found: " + submissionId);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "SUBMISSION_NOT_FOUND"; }
}
