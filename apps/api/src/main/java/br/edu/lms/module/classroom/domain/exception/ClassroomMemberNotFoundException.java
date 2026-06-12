package br.edu.lms.module.classroom.domain.exception;

public class ClassroomMemberNotFoundException extends RuntimeException {
    public ClassroomMemberNotFoundException() {
        super("CLASSROOM_MEMBER_NOT_FOUND");
    }
}
