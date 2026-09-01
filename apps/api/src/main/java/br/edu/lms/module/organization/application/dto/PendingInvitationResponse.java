package br.edu.lms.module.organization.application.dto;

import br.edu.lms.module.organization.domain.model.MemberRole;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class PendingInvitationResponse {
    String token;
    String organizationId;
    String organizationName;
    MemberRole role;
    Instant expiresAt;
}
