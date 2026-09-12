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

    /**
     * O estado que o convite tem de fato. EXPIRED nunca é gravado: um pendente
     * vencido é expirado na leitura, sem job que atualize o banco.
     */
    public InvitationStatus effectiveStatus() {
        return isPending() && isExpired() ? InvitationStatus.EXPIRED : status;
    }

    /** Um convite cancelado deixa de valer: o link não serve mais para entrar. */
    public Invitation cancel() {
        return toBuilder().status(InvitationStatus.CANCELLED).build();
    }

    /** O convite vale para o e-mail a que foi endereçado, comparado sem caixa. */
    public boolean isAddressedTo(String candidateEmail) {
        return candidateEmail != null && email != null
                && email.equalsIgnoreCase(candidateEmail.trim());
    }
}
