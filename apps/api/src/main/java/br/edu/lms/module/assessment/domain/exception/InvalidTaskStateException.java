package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.shared.exception.HttpMappable;

public class InvalidTaskStateException extends RuntimeException implements HttpMappable {
    public InvalidTaskStateException(TaskStatus current, TaskStatus target) {
        super("Cannot transition task from " + current + " to " + target);
    }

    @Override public int httpStatus() { return 409; }
    @Override public String errorCode() { return getMessage(); }
}
