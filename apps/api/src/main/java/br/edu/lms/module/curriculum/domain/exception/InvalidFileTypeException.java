package br.edu.lms.module.curriculum.domain.exception;

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String mimeType) {
        super("File type not allowed: " + mimeType);
    }
}
