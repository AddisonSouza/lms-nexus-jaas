package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.OrganizationInvitationResponse;
import br.edu.lms.module.organization.application.mapper.OrganizationInvitationMapper;
import br.edu.lms.module.organization.domain.model.Invitation;
import br.edu.lms.module.organization.domain.port.in.ListOrganizationInvitationsUseCase;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListOrganizationInvitationsService implements ListOrganizationInvitationsUseCase {

    private final InvitationRepository invitationRepository;
    private final UserDirectoryPort userDirectoryPort;
    private final OrganizationInvitationMapper invitationMapper;

    @Override
    public List<OrganizationInvitationResponse> execute(String organizationId) {
        var invitations = invitationRepository.findByOrganization(organizationId);

        var inviters = userDirectoryPort.findProfilesByIds(
                invitations.stream().map(Invitation::getInvitedBy).distinct().toList());

        return invitations.stream()
                .map(i -> invitationMapper.toResponse(i, inviters.get(i.getInvitedBy())))
                .toList();
    }
}
