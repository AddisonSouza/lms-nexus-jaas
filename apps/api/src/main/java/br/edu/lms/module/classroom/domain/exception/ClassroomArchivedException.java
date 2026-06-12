package br.edu.lms.module.classroom.domain.exception;

public class ClassroomArchivedException extends RuntimeException {
    public ClassroomArchivedException() {
        super("CLASSROOM_ARCHIVED");
    }
}
