package br.edu.lms.module.curriculum.domain.exception;

public class InvalidTeacherAssignmentException extends RuntimeException {
    public InvalidTeacherAssignmentException(String reason) {
        super(reason);
    }
}
