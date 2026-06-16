package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvalidAttachmentTypeException extends RuntimeException implements HttpMappable {
    public InvalidAttachmentTypeException(String mimeType) {
        super("Attachment type not allowed: " + mimeType);
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return getMessage(); }
}
