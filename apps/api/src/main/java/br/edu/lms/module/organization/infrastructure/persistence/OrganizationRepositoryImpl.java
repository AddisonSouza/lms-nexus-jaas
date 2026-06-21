package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.Organization;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final EntityManager em;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public Organization save(Organization org) {
        var entity = organizationMapper.toEntity(org);
        em.merge(entity);
        return org;
    }

    @Override
    public Optional<Organization> findByOwnerIdAndName(String ownerId, String name) {
        return em.createQuery(
                        "SELECT o FROM OrganizationJpaEntity o " +
                        "WHERE o.ownerId = :ownerId AND LOWER(o.name) = LOWER(:name) AND o.deletedAt IS NULL",
                        OrganizationJpaEntity.class)
                .setParameter("ownerId", ownerId)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .map(organizationMapper::toDomain);
    }

    @Override
    public Optional<Organization> findById(OrganizationId id) {
        return Optional.ofNullable(em.find(OrganizationJpaEntity.class, id.getValue()))
                .filter(e -> e.getDeletedAt() == null)
                .map(organizationMapper::toDomain);
    }
}
