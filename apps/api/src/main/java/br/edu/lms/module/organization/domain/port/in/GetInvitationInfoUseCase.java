package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.domain.model.MemberRole;

import java.time.Instant;

public interface GetInvitationInfoUseCase {

    InvitationInfo execute(String token);

    record InvitationInfo(
            String organizationId,
            String organizationName,
            String email,
            MemberRole role,
            Instant expiresAt
    ) {}
}
