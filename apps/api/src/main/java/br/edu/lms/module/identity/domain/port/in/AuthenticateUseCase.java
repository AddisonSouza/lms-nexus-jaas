package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.AuthenticateCommand;
import br.edu.lms.module.identity.application.dto.AuthResult;

public interface AuthenticateUseCase {
    AuthResult execute(AuthenticateCommand command);
}
