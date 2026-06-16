package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubjectTeacherAssignmentNotFoundException extends RuntimeException implements HttpMappable {
    public SubjectTeacherAssignmentNotFoundException() {
        super("SUBJECT_TEACHER_ASSIGNMENT_NOT_FOUND");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "SUBJECT_TEACHER_ASSIGNMENT_NOT_FOUND"; }
}
