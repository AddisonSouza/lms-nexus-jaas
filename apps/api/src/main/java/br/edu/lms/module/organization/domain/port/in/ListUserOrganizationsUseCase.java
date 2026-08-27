package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.UserOrganizationResponse;

import java.util.List;

public interface ListUserOrganizationsUseCase {
    List<UserOrganizationResponse> execute(String userId);
}
