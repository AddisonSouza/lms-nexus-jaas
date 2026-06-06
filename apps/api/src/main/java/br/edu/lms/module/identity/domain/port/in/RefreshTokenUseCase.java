package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.application.dto.RefreshCommand;

public interface RefreshTokenUseCase {
    AuthResult execute(RefreshCommand command);
}
