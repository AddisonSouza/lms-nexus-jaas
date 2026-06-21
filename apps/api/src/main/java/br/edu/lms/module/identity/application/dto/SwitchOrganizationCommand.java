package br.edu.lms.module.identity.application.dto;

public record SwitchOrganizationCommand(String refreshToken, String organizationId) {}
