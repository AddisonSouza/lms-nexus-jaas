package br.edu.lms.module.organization.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AcceptInviteCommand {
    String token;
    String userId;
}
