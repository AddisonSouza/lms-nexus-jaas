package br.edu.lms.module.identity.application.dto;

public record RefreshCommand(String refreshToken, String organizationId) {
    public RefreshCommand(String refreshToken) {
        this(refreshToken, null);
    }
}
