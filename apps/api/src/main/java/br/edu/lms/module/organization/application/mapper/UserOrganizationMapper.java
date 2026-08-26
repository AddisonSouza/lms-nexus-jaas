package br.edu.lms.module.organization.application.mapper;

import br.edu.lms.module.organization.application.dto.UserOrganizationResponse;
import br.edu.lms.module.organization.domain.model.UserOrganization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface UserOrganizationMapper {

    UserOrganizationResponse toResponse(UserOrganization domain);

    List<UserOrganizationResponse> toResponseList(List<UserOrganization> domain);
}
