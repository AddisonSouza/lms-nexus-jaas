package br.edu.lms.module.curriculum.domain.exception;

public class TopicNotFoundException extends RuntimeException {
    public TopicNotFoundException(String id) {
        super("Topic not found: " + id);
    }
}
