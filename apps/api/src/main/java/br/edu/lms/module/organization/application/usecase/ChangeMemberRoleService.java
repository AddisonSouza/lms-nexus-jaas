package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.CannotChangeOwnerRoleException;
import br.edu.lms.module.organization.domain.exception.MemberNotFoundException;
import br.edu.lms.module.organization.domain.exception.RoleNotAssignableException;
import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.event.OrganizationMembershipChangedEvent;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.port.in.ChangeMemberRoleUseCase;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ChangeMemberRoleService implements ChangeMemberRoleUseCase {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final Event<OrganizationMembershipChangedEvent> membershipChangedEvent;

    @Override
    @Transactional
    public void execute(String organizationId, String userId, MemberRole role) {
        if (role == MemberRole.ADMIN_ORG) {
            throw new RoleNotAssignableException();
        }

        var organization = organizationRepository.findById(OrganizationId.of(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        if (organization.getOwnerId().equals(userId)) {
            throw new CannotChangeOwnerRoleException();
        }

        var member = memberRepository.findActiveByOrgAndUser(organizationId, userId)
                .orElseThrow(MemberNotFoundException::new);

        memberRepository.updateRole(member.getId(), role);
        membershipChangedEvent.fire(new OrganizationMembershipChangedEvent(userId, organizationId));
        log.info("Member {} of org {} changed to role {}", userId, organizationId, role);
    }
}
