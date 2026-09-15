package br.edu.lms.module.identity.interfaces.rest.dto;

import br.edu.lms.module.identity.interfaces.rest.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @StrongPassword String newPassword
) {}
