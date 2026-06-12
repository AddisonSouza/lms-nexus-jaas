package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class OrganizationMemberRepositoryImpl implements OrganizationMemberRepository, OrganizationMemberLookupPort {

    private final EntityManager em;

    @Override
    @Transactional
    public OrganizationMember save(OrganizationMember member) {
        var entity = new OrganizationMemberJpaEntity();
        entity.setId(member.getId());
        entity.setOrganizationId(member.getOrganizationId());
        entity.setUserId(member.getUserId());
        entity.setRole(member.getRole().name());
        em.merge(entity);
        return member;
    }

    @Override
    public Optional<String> findRoleByUserAndOrg(String userId, String organizationId) {
        return em.createQuery(
                        "SELECT m.role FROM OrganizationMemberJpaEntity m " +
                        "WHERE m.userId = :userId AND m.organizationId = :orgId AND m.deletedAt IS NULL",
                        String.class)
                .setParameter("userId", userId)
                .setParameter("orgId", organizationId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public boolean existsActiveMemberByEmail(String organizationId, String email) {
        var count = ((Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM organization_members om " +
                        "JOIN users u ON u.id = om.user_id " +
                        "WHERE om.organization_id = :orgId AND LOWER(u.email) = LOWER(:email) " +
                        "AND om.deleted_at IS NULL")
                .setParameter("orgId", organizationId)
                .setParameter("email", email)
                .getSingleResult()).longValue();
        return count > 0;
    }

    @Override
    public boolean existsActiveByOrgAndUser(String organizationId, String userId) {
        return em.createQuery(
                        "SELECT COUNT(m) FROM OrganizationMemberJpaEntity m " +
                        "WHERE m.organizationId = :orgId AND m.userId = :userId AND m.deletedAt IS NULL",
                        Long.class)
                .setParameter("orgId", organizationId)
                .setParameter("userId", userId)
                .getSingleResult() > 0;
    }

    @Override
    public Optional<OrganizationMember> findActiveByOrgAndUser(String organizationId, String userId) {
        return em.createQuery(
                        "SELECT m FROM OrganizationMemberJpaEntity m " +
                        "WHERE m.organizationId = :orgId AND m.userId = :userId AND m.deletedAt IS NULL",
                        OrganizationMemberJpaEntity.class)
                .setParameter("orgId", organizationId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .map(e -> OrganizationMember.builder()
                        .id(e.getId())
                        .organizationId(e.getOrganizationId())
                        .userId(e.getUserId())
                        .role(MemberRole.valueOf(e.getRole()))
                        .build());
    }

    @Override
    @Transactional
    public void softDelete(String memberId) {
        em.createQuery(
                        "UPDATE OrganizationMemberJpaEntity m SET m.deletedAt = :now WHERE m.id = :id")
                .setParameter("now", java.time.LocalDateTime.now())
                .setParameter("id", memberId)
                .executeUpdate();
    }
}
