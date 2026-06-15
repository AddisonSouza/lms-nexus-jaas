package br.edu.lms.module.curriculum.domain.exception;

public class TopicAccessDeniedException extends RuntimeException {
    public TopicAccessDeniedException() {
        super("Access denied to topic resource");
    }
}
