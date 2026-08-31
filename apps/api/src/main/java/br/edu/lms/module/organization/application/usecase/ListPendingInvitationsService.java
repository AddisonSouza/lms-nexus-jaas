package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.PendingInvitationResponse;
import br.edu.lms.module.organization.application.mapper.PendingInvitationMapper;
import br.edu.lms.module.organization.domain.model.Invitation;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.port.in.ListPendingInvitationsUseCase;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListPendingInvitationsService implements ListPendingInvitationsUseCase {

    private final InvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserDirectoryPort userDirectoryPort;
    private final PendingInvitationMapper pendingInvitationMapper;

    @Override
    public List<PendingInvitationResponse> execute(String userId) {
        return userDirectoryPort.findEmailById(userId)
                .map(invitationRepository::findPendingByEmail)
                .orElseGet(List::of)
                .stream()
                .map(this::toResponse)
                // Convite de organização removida não tem para onde levar o usuário.
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private PendingInvitationResponse toResponse(Invitation invitation) {
        return organizationRepository
                .findById(OrganizationId.of(invitation.getOrganizationId()))
                .map(org -> pendingInvitationMapper.toResponse(invitation, org.getName()))
                .orElse(null);
    }
}
