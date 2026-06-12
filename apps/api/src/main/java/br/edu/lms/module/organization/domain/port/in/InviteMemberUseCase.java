package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.InviteMemberCommand;

public interface InviteMemberUseCase {
    void execute(InviteMemberCommand command);
}
