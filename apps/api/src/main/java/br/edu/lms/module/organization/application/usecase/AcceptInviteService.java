package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.AcceptInviteCommand;
import br.edu.lms.module.organization.domain.exception.*;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.in.AcceptInviteUseCase;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AcceptInviteService implements AcceptInviteUseCase {

    private final InvitationRepository invitationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserDirectoryPort userDirectory;

    @Override
    @Transactional
    public void execute(AcceptInviteCommand command) {
        var invitation = invitationRepository.findByToken(command.getToken())
                .orElseThrow(InvitationNotFoundException::new);

        if (invitation.getStatus() == InvitationStatus.CANCELLED) {
            throw new InvitationCancelledException();
        }
        if (!invitation.isPending()) {
            throw new InvitationAlreadyUsedException();
        }
        if (invitation.isExpired()) {
            throw new InvitationExpiredException();
        }
        // O link é secreto, mas não é uma credencial: só entra quem foi convidado.
        var email = userDirectory.findEmailById(command.getUserId())
                .orElseThrow(InvitationNotForThisUserException::new);
        if (!invitation.isAddressedTo(email)) {
            throw new InvitationNotForThisUserException();
        }
        if (memberRepository.existsActiveByOrgAndUser(invitation.getOrganizationId(), command.getUserId())) {
            throw new AlreadyAMemberException();
        }

        // Remover é soft delete: a linha continua, e uq_member (org, user) não aceita
        // outra. Quem volta tem o vínculo antigo reativado com o papel deste convite.
        var removed = memberRepository.findRemovedByOrgAndUser(invitation.getOrganizationId(), command.getUserId());
        if (removed.isPresent()) {
            memberRepository.reactivate(removed.get().getId(), invitation.getRole());
        } else {
            memberRepository.save(OrganizationMember.builder()
                    .id(UUID.randomUUID().toString())
                    .organizationId(invitation.getOrganizationId())
                    .userId(command.getUserId())
                    .role(invitation.getRole())
                    .build());
        }

        var used = invitation.toBuilder().status(InvitationStatus.USED).build();
        invitationRepository.save(used);

        log.info("Invitation {} accepted by user {}", command.getToken(), command.getUserId());
    }
}
