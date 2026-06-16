package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class TopicAccessDeniedException extends RuntimeException implements HttpMappable {
    public TopicAccessDeniedException() {
        super("Access denied to topic resource");
    }

    @Override public int httpStatus() { return 403; }
    @Override public String errorCode() { return "TOPIC_ACCESS_DENIED"; }
}
