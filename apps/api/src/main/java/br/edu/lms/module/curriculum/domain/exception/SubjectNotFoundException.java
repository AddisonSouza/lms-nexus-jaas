package br.edu.lms.module.curriculum.domain.exception;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException() {
        super("SUBJECT_NOT_FOUND");
    }

    public SubjectNotFoundException(String subjectId) {
        super("SUBJECT_NOT_FOUND: " + subjectId);
    }
}
