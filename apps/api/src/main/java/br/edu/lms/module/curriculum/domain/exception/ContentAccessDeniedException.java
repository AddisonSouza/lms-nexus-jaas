package br.edu.lms.module.curriculum.domain.exception;

public class ContentAccessDeniedException extends RuntimeException {
    public ContentAccessDeniedException() {
        super("Access denied to subject content");
    }
}
