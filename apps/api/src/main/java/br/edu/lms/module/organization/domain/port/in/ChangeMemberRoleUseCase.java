package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.domain.model.MemberRole;

public interface ChangeMemberRoleUseCase {
    void execute(String organizationId, String userId, MemberRole role);
}
