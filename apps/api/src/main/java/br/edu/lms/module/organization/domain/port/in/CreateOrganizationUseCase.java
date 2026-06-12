package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.CreateOrganizationCommand;
import br.edu.lms.module.organization.application.dto.OrganizationResponse;

public interface CreateOrganizationUseCase {
    OrganizationResponse execute(CreateOrganizationCommand command);
}
