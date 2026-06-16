package br.edu.lms.module.curriculum.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class SubjectClassroomLinkNotFoundException extends RuntimeException implements HttpMappable {
    public SubjectClassroomLinkNotFoundException() {
        super("SUBJECT_CLASSROOM_LINK_NOT_FOUND");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "SUBJECT_CLASSROOM_LINK_NOT_FOUND"; }
}
