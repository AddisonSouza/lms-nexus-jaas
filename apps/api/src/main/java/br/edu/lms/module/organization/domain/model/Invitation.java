package br.edu.lms.module.organization.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Invitation {

    @EqualsAndHashCode.Include
    private final InvitationId id;

    private final String organizationId;
    private final String email;
    private final MemberRole role;
    private final String token;
    private final InvitationStatus status;
    private final String invitedBy;
    private final Instant expiresAt;
    private final Instant createdAt;

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
