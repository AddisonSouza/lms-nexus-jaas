package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.InvitationNotFoundException;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.port.in.GetInvitationInfoUseCase;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetInvitationInfoService implements GetInvitationInfoUseCase {

    private final InvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;

    @Override
    public InvitationInfo execute(String token) {
        var invitation = invitationRepository.findByToken(token)
                .orElseThrow(InvitationNotFoundException::new);

        var org = organizationRepository.findById(OrganizationId.of(invitation.getOrganizationId()))
                .orElseThrow(() -> new IllegalStateException("Organization not found for invitation"));

        return new InvitationInfo(
                org.getId().getValue(),
                org.getName(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getExpiresAt()
        );
    }
}
