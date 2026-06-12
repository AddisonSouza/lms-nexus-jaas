package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.Organization;
import br.edu.lms.module.organization.domain.model.OrganizationId;

import java.util.Optional;

public interface OrganizationRepository {
    Organization save(Organization organization);
    Optional<Organization> findByOwnerIdAndName(String ownerId, String name);
    Optional<Organization> findById(OrganizationId id);
}
