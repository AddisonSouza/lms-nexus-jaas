package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.InviteMemberCommand;
import br.edu.lms.module.organization.domain.exception.AlreadyAMemberException;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.in.InviteMemberUseCase;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import br.edu.lms.module.organization.domain.event.MemberInvitedEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class InviteMemberService implements InviteMemberUseCase {

    private final InvitationRepository invitationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final Event<MemberInvitedEvent> memberInvitedEvent;

    @Override
    public void execute(InviteMemberCommand command) {
        if (memberRepository.existsActiveMemberByEmail(command.getOrganizationId(), command.getEmail())) {
            throw new AlreadyAMemberException();
        }

        var token = UUID.randomUUID().toString();
        var invitation = Invitation.builder()
                .id(InvitationId.generate())
                .organizationId(command.getOrganizationId())
                .email(command.getEmail())
                .role(command.getRole())
                .token(token)
                .status(InvitationStatus.PENDING)
                .invitedBy(command.getInvitedBy())
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();

        invitationRepository.save(invitation);
        memberInvitedEvent.fire(new MemberInvitedEvent(command.getOrganizationId(), command.getEmail(), token));
        log.info("Invitation sent to {} for org {}", command.getEmail(), command.getOrganizationId());
    }
}
