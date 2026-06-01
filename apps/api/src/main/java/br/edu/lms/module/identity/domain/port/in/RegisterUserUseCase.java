package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.RegisterUserCommand;
import br.edu.lms.module.identity.application.dto.RegisterUserResponse;

public interface RegisterUserUseCase {
    RegisterUserResponse execute(RegisterUserCommand command);
}
