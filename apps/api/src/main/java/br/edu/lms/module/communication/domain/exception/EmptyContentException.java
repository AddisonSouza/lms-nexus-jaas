package br.edu.lms.module.communication.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class EmptyContentException extends RuntimeException implements HttpMappable {
    public EmptyContentException() {
        super("Announcement content is required");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "EMPTY_ANNOUNCEMENT_CONTENT"; }
}
