package br.edu.lms.module.classroom.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class ClassroomArchivedException extends RuntimeException implements HttpMappable {
    public ClassroomArchivedException() {
        super("CLASSROOM_ARCHIVED");
    }

    @Override public int httpStatus() { return 422; }
    @Override public String errorCode() { return "CLASSROOM_ARCHIVED"; }
}
