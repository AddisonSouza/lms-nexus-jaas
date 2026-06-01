package br.edu.lms.module.identity.application.dto;

import lombok.Builder;

@Builder
public record RegisterUserCommand(String fullName, String email, String rawPassword) {
}
