package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

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
    public boolean hasProfessorRole(String memberId, String organizationId) {
        var count = em.createQuery(
                        "SELECT COUNT(m) FROM br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity m " +
                        "WHERE m.id = :id AND m.organizationId = :orgId AND m.role = 'PROFESSOR' AND m.deletedAt IS NULL",
                        Long.class)
                .setParameter("id", memberId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }
}
