package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class TopicNotFoundException extends RuntimeException implements HttpMappable {
    public TopicNotFoundException(String id) {
        super("Topic not found: " + id);
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "TOPIC_NOT_FOUND"; }
}
