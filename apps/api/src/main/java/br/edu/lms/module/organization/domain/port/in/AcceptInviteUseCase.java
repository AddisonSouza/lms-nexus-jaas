package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.AcceptInviteCommand;

public interface AcceptInviteUseCase {
    void execute(AcceptInviteCommand command);
}
