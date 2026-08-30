package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.OrganizationMemberResponse;

import java.util.List;

public interface ListOrganizationMembersUseCase {
    List<OrganizationMemberResponse> execute(String organizationId);
}
