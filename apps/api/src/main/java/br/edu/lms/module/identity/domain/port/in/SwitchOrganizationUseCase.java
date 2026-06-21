package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.application.dto.SwitchOrganizationCommand;

public interface SwitchOrganizationUseCase {
    AuthResult execute(SwitchOrganizationCommand command);
}
