package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.Invitation;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class InvitationRepositoryImpl implements InvitationRepository {

    private final EntityManager em;
    private final InvitationMapper invitationMapper;

    @Override
    @Transactional
    public void save(Invitation invitation) {
        var entity = invitationMapper.toEntity(invitation);
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
                .map(invitationMapper::toDomain);
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

    @Override
    public List<Invitation> findPendingByEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        return em.createQuery(
                        "SELECT i FROM InvitationJpaEntity i " +
                        "WHERE LOWER(i.email) = LOWER(:email) " +
                        "AND i.status = 'PENDING' AND i.expiresAt > :now " +
                        "ORDER BY i.createdAt DESC",
                        InvitationJpaEntity.class)
                .setParameter("email", email.trim())
                .setParameter("now", LocalDateTime.now())
                .getResultStream()
                .map(invitationMapper::toDomain)
                .toList();
    }
}
