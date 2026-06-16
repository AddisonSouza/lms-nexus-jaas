package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubjectNotFoundException extends RuntimeException implements HttpMappable {
    public SubjectNotFoundException() {
        super("SUBJECT_NOT_FOUND");
    }

    public SubjectNotFoundException(String subjectId) {
        super("SUBJECT_NOT_FOUND: " + subjectId);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "SUBJECT_NOT_FOUND"; }
}
