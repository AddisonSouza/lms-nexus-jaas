package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class InvalidFileTypeException extends RuntimeException implements HttpMappable {
    public InvalidFileTypeException(String mimeType) {
        super("File type not allowed: " + mimeType);
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return getMessage(); }
}
