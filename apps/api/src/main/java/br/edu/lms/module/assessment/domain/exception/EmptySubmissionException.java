package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class EmptySubmissionException extends RuntimeException implements HttpMappable {
    public EmptySubmissionException() {
        super("Submission must have text or at least one file");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "EMPTY_SUBMISSION"; }
}
