package br.edu.lms.module.identity.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequestPasswordResetCommand {
    String email;
}
