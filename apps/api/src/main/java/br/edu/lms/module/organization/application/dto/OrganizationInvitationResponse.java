package br.edu.lms.module.organization.application.dto;

import br.edu.lms.module.organization.domain.model.InvitationStatus;
import br.edu.lms.module.organization.domain.model.MemberRole;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/** Um convite visto pelo admin da organização. Sem o token: o link é segredo do convidado. */
@Value
@Builder
public class OrganizationInvitationResponse {
    String id;
    String email;
    MemberRole role;
    InvitationStatus status;
    String invitedByName;
    Instant createdAt;
    Instant expiresAt;
}
