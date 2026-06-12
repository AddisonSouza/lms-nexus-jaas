package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.ResendConfirmationCommand;

public interface ResendConfirmationUseCase {
    void execute(ResendConfirmationCommand command);
}
