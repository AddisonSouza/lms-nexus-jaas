package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.OrganizationInvitationResponse;

import java.util.List;

public interface ListOrganizationInvitationsUseCase {

    /** Convites da organização em qualquer estado, do mais recente para o mais antigo. */
    List<OrganizationInvitationResponse> execute(String organizationId);
}
