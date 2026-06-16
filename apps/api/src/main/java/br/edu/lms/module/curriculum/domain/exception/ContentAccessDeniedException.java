package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class ContentAccessDeniedException extends RuntimeException implements HttpMappable {
    public ContentAccessDeniedException() {
        super("Access denied to subject content");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "CONTENT_ACCESS_DENIED"; }
}
