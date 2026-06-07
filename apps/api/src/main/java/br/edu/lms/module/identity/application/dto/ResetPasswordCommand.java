package br.edu.lms.module.identity.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResetPasswordCommand {
    String token;
    String newPassword;
}
