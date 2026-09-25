package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class OrganizationMemberQueryPortImpl implements OrganizationMemberQueryPort {

    private final EntityManager em;

    @Override
    public boolean existsByIdAndOrganizationId(String memberId, String organizationId) {
        var count = em.createQuery(
                        "SELECT COUNT(m) FROM br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity m " +
                        "WHERE m.id = :id AND m.organizationId = :orgId AND m.deletedAt IS NULL",
                        Long.class)
                .setParameter("id", memberId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean canTeach(String memberId, String organizationId) {
        var count = em.createQuery(
                        "SELECT COUNT(m) FROM br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity m " +
                        "WHERE m.id = :id AND m.organizationId = :orgId AND m.role IN ('PROFESSOR', 'GESTOR', 'ADMIN_ORG') AND m.deletedAt IS NULL",
                        Long.class)
                .setParameter("id", memberId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public List<String> findUserIdsByMemberIds(List<String> memberIds, String organizationId) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT m.userId FROM br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity m " +
                        "WHERE m.id IN :ids AND m.organizationId = :orgId AND m.deletedAt IS NULL",
                        String.class)
                .setParameter("ids", memberIds)
                .setParameter("orgId", organizationId)
                .getResultList();
    }

    @Override
    public List<String> findActiveUserIdsByMemberIds(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT m.userId FROM br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity m " +
                        "WHERE m.id IN :ids AND m.deletedAt IS NULL",
                        String.class)
                .setParameter("ids", memberIds)
                .getResultList();
    }
}
