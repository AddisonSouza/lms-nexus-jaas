package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.InvitationNotFoundException;
import br.edu.lms.module.organization.domain.exception.InvitationNotPendingException;
import br.edu.lms.module.organization.domain.model.InvitationId;
import br.edu.lms.module.organization.domain.model.InvitationStatus;
import br.edu.lms.module.organization.domain.port.in.CancelInvitationUseCase;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CancelInvitationService implements CancelInvitationUseCase {

    private final InvitationRepository invitationRepository;

    @Override
    @Transactional
    public void execute(String organizationId, String invitationId) {
        // Convite de outra organização responde como inexistente, sem revelar que existe.
        var invitation = invitationRepository.findByIdInOrganization(InvitationId.of(invitationId), organizationId)
                .orElseThrow(InvitationNotFoundException::new);

        // Aceito, cancelado ou vencido já não tem link valendo para desfazer.
        if (invitation.effectiveStatus() != InvitationStatus.PENDING) {
            throw new InvitationNotPendingException();
        }

        invitationRepository.save(invitation.cancel());
        log.info("Invitation {} of org {} cancelled", invitationId, organizationId);
    }
}
