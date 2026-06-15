package br.edu.lms.module.curriculum.domain.exception;

public class SubjectClassroomLinkNotFoundException extends RuntimeException {
    public SubjectClassroomLinkNotFoundException() {
        super("SUBJECT_CLASSROOM_LINK_NOT_FOUND");
    }
}
