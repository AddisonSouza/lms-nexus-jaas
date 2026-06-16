package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubjectAlreadyLinkedException extends RuntimeException implements HttpMappable {
    public SubjectAlreadyLinkedException() {
        super("SUBJECT_ALREADY_LINKED");
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return "SUBJECT_ALREADY_LINKED"; }
}
