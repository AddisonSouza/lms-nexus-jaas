package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class InvitationRepositoryImpl implements InvitationRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public void save(Invitation invitation) {
        var entity = toEntity(invitation);
        em.merge(entity);
    }

    @Override
    public Optional<Invitation> findByToken(String token) {
        return em.createQuery(
                        "SELECT i FROM InvitationJpaEntity i WHERE i.token = :token",
                        InvitationJpaEntity.class)
                .setParameter("token", token)
                .getResultStream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public boolean existsActiveByOrgAndEmail(String organizationId, String email) {
        return em.createQuery(
                        "SELECT COUNT(i) FROM InvitationJpaEntity i " +
                        "WHERE i.organizationId = :orgId AND LOWER(i.email) = LOWER(:email) " +
                        "AND i.status = 'PENDING' AND i.expiresAt > :now",
                        Long.class)
                .setParameter("orgId", organizationId)
                .setParameter("email", email)
                .setParameter("now", LocalDateTime.now())
                .getSingleResult() > 0;
    }

    private InvitationJpaEntity toEntity(Invitation inv) {
        var e = new InvitationJpaEntity();
        e.setId(inv.getId().getValue());
        e.setOrganizationId(inv.getOrganizationId());
        e.setEmail(inv.getEmail());
        e.setRole(inv.getRole().name());
        e.setToken(inv.getToken());
        e.setStatus(inv.getStatus().name());
        e.setInvitedBy(inv.getInvitedBy());
        e.setExpiresAt(LocalDateTime.ofInstant(inv.getExpiresAt(), ZoneOffset.UTC));
        if (inv.getCreatedAt() != null) {
            e.setCreatedAt(LocalDateTime.ofInstant(inv.getCreatedAt(), ZoneOffset.UTC));
        }
        return e;
    }

    private Invitation toDomain(InvitationJpaEntity e) {
        return Invitation.builder()
                .id(InvitationId.of(e.getId()))
                .organizationId(e.getOrganizationId())
                .email(e.getEmail())
                .role(MemberRole.valueOf(e.getRole()))
                .token(e.getToken())
                .status(InvitationStatus.valueOf(e.getStatus()))
                .invitedBy(e.getInvitedBy())
                .expiresAt(e.getExpiresAt().toInstant(ZoneOffset.UTC))
                .createdAt(e.getCreatedAt().toInstant(ZoneOffset.UTC))
                .build();
    }
}
