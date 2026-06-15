package br.edu.lms.module.curriculum.domain.exception;

public class SubjectTeacherAssignmentNotFoundException extends RuntimeException {
    public SubjectTeacherAssignmentNotFoundException() {
        super("SUBJECT_TEACHER_ASSIGNMENT_NOT_FOUND");
    }
}
