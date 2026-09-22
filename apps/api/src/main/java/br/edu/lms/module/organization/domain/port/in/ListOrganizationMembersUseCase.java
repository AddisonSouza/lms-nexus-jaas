package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.OrganizationMemberResponse;
import br.edu.lms.shared.domain.Page;

import java.util.List;

public interface ListOrganizationMembersUseCase {
    List<OrganizationMemberResponse> execute(String organizationId);

    /**
     * Página de membros filtrada por nome ou e-mail e já ordenada por nome pelo
     * banco. {@code search} nulo ou em branco não filtra.
     */
    Page<OrganizationMemberResponse> execute(String organizationId, String search, int page, int size);
}
