package br.edu.lms.module.organization.application.mapper;

import br.edu.lms.module.organization.application.dto.OrganizationMemberResponse;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface OrganizationMemberMapper {

    @Mapping(target = "id", source = "member.id")
    @Mapping(target = "userId", source = "member.userId")
    @Mapping(target = "role", source = "member.role")
    @Mapping(target = "joinedAt", source = "member.joinedAt")
    @Mapping(target = "name", source = "profile.fullName")
    @Mapping(target = "email", source = "profile.email")
    @Mapping(target = "owner", source = "owner")
    OrganizationMemberResponse toResponse(OrganizationMember member, UserProfile profile, boolean owner);
}
