package br.edu.lms.module.assessment.domain.exception;

import br.edu.lms.module.assessment.domain.model.TaskStatus;

public class InvalidTaskStateException extends RuntimeException {
    public InvalidTaskStateException(TaskStatus current, TaskStatus target) {
        super("Cannot transition task from " + current + " to " + target);
    }
}
