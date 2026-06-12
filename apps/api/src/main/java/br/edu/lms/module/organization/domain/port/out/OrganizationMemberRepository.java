package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.OrganizationMember;

public interface OrganizationMemberRepository {
    OrganizationMember save(OrganizationMember member);
}
