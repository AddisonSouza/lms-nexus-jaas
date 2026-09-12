package br.edu.lms.module.organization.application.mapper;

import br.edu.lms.module.organization.application.dto.OrganizationInvitationResponse;
import br.edu.lms.module.organization.domain.model.Invitation;
import br.edu.lms.module.organization.domain.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface OrganizationInvitationMapper {

    @Mapping(target = "id", source = "invitation.id.value")
    @Mapping(target = "email", source = "invitation.email")
    @Mapping(target = "role", source = "invitation.role")
    @Mapping(target = "status", expression = "java(invitation.effectiveStatus())")
    @Mapping(target = "invitedByName", source = "inviter.fullName")
    @Mapping(target = "createdAt", source = "invitation.createdAt")
    @Mapping(target = "expiresAt", source = "invitation.expiresAt")
    OrganizationInvitationResponse toResponse(Invitation invitation, UserProfile inviter);
}
