package br.edu.lms.module.identity.domain.port.in;

import br.edu.lms.module.identity.application.dto.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void execute(ResetPasswordCommand command);
}
