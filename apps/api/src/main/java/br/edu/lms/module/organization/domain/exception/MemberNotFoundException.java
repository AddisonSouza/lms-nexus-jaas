package br.edu.lms.module.organization.domain.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("Member not found in this organization");
    }
}
