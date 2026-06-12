package br.edu.lms.module.identity.domain.port.out;

import java.util.Optional;

public interface OrganizationMemberLookupPort {
    Optional<String> findRoleByUserAndOrg(String userId, String organizationId);
}
