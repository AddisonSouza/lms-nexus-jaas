package br.edu.lms.module.classroom.domain.exception;

public class ClassroomNotFoundException extends RuntimeException {
    public ClassroomNotFoundException() {
        super("CLASSROOM_NOT_FOUND");
    }
}
