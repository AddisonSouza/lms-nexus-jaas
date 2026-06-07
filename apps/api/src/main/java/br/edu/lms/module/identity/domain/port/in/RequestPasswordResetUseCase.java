package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.RequestPasswordResetCommand;

public interface RequestPasswordResetUseCase {
    void execute(RequestPasswordResetCommand command);
}
