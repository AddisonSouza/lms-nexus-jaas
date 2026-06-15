package br.edu.lms.module.curriculum.domain.exception;

public class ContentNotFoundException extends RuntimeException {
    public ContentNotFoundException(String id) {
        super("Content not found: " + id);
    }
}
