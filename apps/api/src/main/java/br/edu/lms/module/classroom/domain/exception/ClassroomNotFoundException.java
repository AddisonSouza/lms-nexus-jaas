package br.edu.lms.module.classroom.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class ClassroomNotFoundException extends RuntimeException implements HttpMappable {
    public ClassroomNotFoundException() {
        super("CLASSROOM_NOT_FOUND");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "CLASSROOM_NOT_FOUND"; }
}
