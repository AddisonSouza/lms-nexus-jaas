package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.CannotRemoveOwnerException;
import br.edu.lms.module.organization.domain.exception.MemberNotFoundException;
import br.edu.lms.module.organization.domain.event.OrganizationMembershipChangedEvent;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.port.in.RemoveMemberUseCase;
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
public class RemoveMemberService implements RemoveMemberUseCase {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final Event<OrganizationMembershipChangedEvent> membershipChangedEvent;

    @Override
    @Transactional
    public void execute(String organizationId, String userId) {
        var organization = organizationRepository.findById(OrganizationId.of(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        if (organization.getOwnerId().equals(userId)) {
            throw new CannotRemoveOwnerException();
        }

        var member = memberRepository.findActiveByOrgAndUser(organizationId, userId)
                .orElseThrow(MemberNotFoundException::new);

        memberRepository.softDelete(member.getId());
        membershipChangedEvent.fire(new OrganizationMembershipChangedEvent(userId, organizationId));
        log.info("Member {} removed from org {}", userId, organizationId);
    }
}
