package br.edu.lms.module.identity.application.dto;

import br.edu.lms.module.identity.domain.model.UserStatus;
import lombok.Builder;

@Builder
public record RegisterUserResponse(String userId, String email, UserStatus status) {
}
