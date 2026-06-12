package br.edu.lms.module.identity.interfaces.rest.dto;

public record RefreshRequest(String organizationId) {
    public RefreshRequest() {
        this(null);
    }
}
