package br.edu.lms.module.curriculum.domain.exception;

public class SubjectAlreadyLinkedException extends RuntimeException {
    public SubjectAlreadyLinkedException() {
        super("SUBJECT_ALREADY_LINKED");
    }
}
