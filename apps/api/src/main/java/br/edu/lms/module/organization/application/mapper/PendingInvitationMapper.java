package br.edu.lms.module.organization.application.mapper;

import br.edu.lms.module.organization.application.dto.PendingInvitationResponse;
import br.edu.lms.module.organization.domain.model.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface PendingInvitationMapper {

    @Mapping(target = "token", source = "invitation.token")
    @Mapping(target = "organizationId", source = "invitation.organizationId")
    @Mapping(target = "role", source = "invitation.role")
    @Mapping(target = "expiresAt", source = "invitation.expiresAt")
    @Mapping(target = "organizationName", source = "organizationName")
    PendingInvitationResponse toResponse(Invitation invitation, String organizationName);
}
