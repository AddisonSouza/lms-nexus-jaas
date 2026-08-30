package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.model.UserOrganization;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository {
    OrganizationMember save(OrganizationMember member);
    boolean existsActiveMemberByEmail(String organizationId, String email);
    boolean existsActiveByOrgAndUser(String organizationId, String userId);
    Optional<OrganizationMember> findActiveByOrgAndUser(String organizationId, String userId);
    List<OrganizationMember> findActiveMembersByOrganization(String organizationId);
    List<UserOrganization> findUserOrganizations(String userId);
    void softDelete(String memberId);
}
