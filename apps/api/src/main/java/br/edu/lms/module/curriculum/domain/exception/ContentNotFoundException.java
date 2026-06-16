package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class ContentNotFoundException extends RuntimeException implements HttpMappable {
    public ContentNotFoundException(String id) {
        super("Content not found: " + id);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "CONTENT_NOT_FOUND"; }
}
