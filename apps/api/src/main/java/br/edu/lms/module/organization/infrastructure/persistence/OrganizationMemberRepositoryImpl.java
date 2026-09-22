package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.identity.domain.model.OrgMembership;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.model.UserOrganization;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.shared.domain.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class OrganizationMemberRepositoryImpl implements OrganizationMemberRepository, OrganizationMemberLookupPort {

    // Nome e e-mail vivem no identity; referenciados por FQN como no UserDirectoryAdapter,
    // para filtrar e ordenar no banco sem depender das classes daquele módulo.
    private static final String USER_ENTITY =
            "br.edu.lms.module.identity.infrastructure.persistence.UserJpaEntity";

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

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
    public List<OrgMembership> findOrganizationsByUser(String userId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT m.organizationId, m.role FROM OrganizationMemberJpaEntity m " +
                        "JOIN OrganizationJpaEntity o ON o.id = m.organizationId " +
                        "WHERE m.userId = :userId AND m.deletedAt IS NULL AND o.deletedAt IS NULL " +
                        "ORDER BY o.name",
                        Tuple.class)
                .setParameter("userId", userId)
                .getResultList();

        return rows.stream()
                .map(row -> new OrgMembership(row.get(0, String.class), row.get(1, String.class)))
                .toList();
    }

    @Override
    public List<UserOrganization> findUserOrganizations(String userId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT o.id, o.name, m.role FROM OrganizationMemberJpaEntity m " +
                        "JOIN OrganizationJpaEntity o ON o.id = m.organizationId " +
                        "WHERE m.userId = :userId AND m.deletedAt IS NULL AND o.deletedAt IS NULL " +
                        "ORDER BY o.name",
                        Tuple.class)
                .setParameter("userId", userId)
                .getResultList();

        return rows.stream()
                .map(row -> UserOrganization.builder()
                        .id(row.get(0, String.class))
                        .name(row.get(1, String.class))
                        .role(MemberRole.valueOf(row.get(2, String.class)))
                        .build())
                .toList();
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
                .map(this::toDomain);
    }

    @Override
    public Page<OrganizationMember> searchActiveMembers(String organizationId, String search, int page, int size) {
        int pageNumber = Math.max(page, 0);
        int pageSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        var term = likeTerm(search);
        var filter = term == null
                ? ""
                : " AND (LOWER(u.fullName) LIKE :term OR LOWER(u.email) LIKE :term)";

        var countQuery = em.createQuery(
                        "SELECT COUNT(m) FROM OrganizationMemberJpaEntity m " +
                        "JOIN " + USER_ENTITY + " u ON u.id = m.userId " +
                        "WHERE m.organizationId = :orgId AND m.deletedAt IS NULL" + filter,
                        Long.class)
                .setParameter("orgId", organizationId);

        var pageQuery = em.createQuery(
                        "SELECT m FROM OrganizationMemberJpaEntity m " +
                        "JOIN " + USER_ENTITY + " u ON u.id = m.userId " +
                        "WHERE m.organizationId = :orgId AND m.deletedAt IS NULL" + filter +
                        " ORDER BY u.fullName",
                        OrganizationMemberJpaEntity.class)
                .setParameter("orgId", organizationId);

        if (term != null) {
            countQuery.setParameter("term", term);
            pageQuery.setParameter("term", term);
        }

        var content = pageQuery
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize)
                .getResultStream()
                .map(this::toDomain)
                .toList();

        return Page.of(content, countQuery.getSingleResult(), pageNumber, pageSize);
    }

    /** {@code null} quando não há busca; senão o termo em minúsculas entre curingas. */
    private static String likeTerm(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return "%" + search.trim().toLowerCase() + "%";
    }

    @Override
    public Optional<OrganizationMember> findRemovedByOrgAndUser(String organizationId, String userId) {
        return em.createQuery(
                        "SELECT m FROM OrganizationMemberJpaEntity m " +
                        "WHERE m.organizationId = :orgId AND m.userId = :userId AND m.deletedAt IS NOT NULL",
                        OrganizationMemberJpaEntity.class)
                .setParameter("orgId", organizationId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void reactivate(String memberId, MemberRole role) {
        // UPDATE explícito: joined_at é updatable = false na entidade, e um merge não
        // gravaria a data da volta.
        em.createQuery(
                        "UPDATE OrganizationMemberJpaEntity m " +
                        "SET m.deletedAt = NULL, m.role = :role, m.joinedAt = :now WHERE m.id = :id")
                .setParameter("role", role.name())
                .setParameter("now", java.time.LocalDateTime.now())
                .setParameter("id", memberId)
                .executeUpdate();
    }

    private OrganizationMember toDomain(OrganizationMemberJpaEntity e) {
        return OrganizationMember.builder()
                .id(e.getId())
                .organizationId(e.getOrganizationId())
                .userId(e.getUserId())
                .role(MemberRole.valueOf(e.getRole()))
                .joinedAt(e.getJoinedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateRole(String memberId, MemberRole role) {
        em.createQuery("UPDATE OrganizationMemberJpaEntity m SET m.role = :role WHERE m.id = :id")
                .setParameter("role", role.name())
                .setParameter("id", memberId)
                .executeUpdate();
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
