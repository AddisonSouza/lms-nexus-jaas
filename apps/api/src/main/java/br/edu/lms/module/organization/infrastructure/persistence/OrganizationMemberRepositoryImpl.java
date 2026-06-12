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
}
