package br.edu.lms.module.classroom.domain.exception;

import br.edu.lms.shared.exception.HttpMappable;

public class ClassroomMemberNotFoundException extends RuntimeException implements HttpMappable {
    public ClassroomMemberNotFoundException() {
        super("CLASSROOM_MEMBER_NOT_FOUND");
    }

    @Override public int httpStatus() { return 404; }
    @Override public String errorCode() { return "CLASSROOM_MEMBER_NOT_FOUND"; }
}
