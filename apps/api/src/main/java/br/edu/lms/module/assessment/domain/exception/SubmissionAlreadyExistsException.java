package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubmissionAlreadyExistsException extends RuntimeException implements HttpMappable {
    public SubmissionAlreadyExistsException(String taskId, String studentId) {
        super("Submission already exists for task " + taskId + " by student " + studentId);
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "SUBMISSION_ALREADY_EXISTS"; }
}
