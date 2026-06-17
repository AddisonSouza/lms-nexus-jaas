package br.edu.lms.module.identity.domain.port.out;

import br.edu.lms.module.identity.domain.model.OrgMembership;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberLookupPort {
    Optional<String> findRoleByUserAndOrg(String userId, String organizationId);
    List<OrgMembership> findOrganizationsByUser(String userId);
}
