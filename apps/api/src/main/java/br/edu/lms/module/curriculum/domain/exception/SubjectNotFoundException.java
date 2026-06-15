package br.edu.lms.module.curriculum.domain.exception;

public class SubjectNotFoundException extends RuntimeException {
    public SubjectNotFoundException() {
        super("SUBJECT_NOT_FOUND");
    }
}
