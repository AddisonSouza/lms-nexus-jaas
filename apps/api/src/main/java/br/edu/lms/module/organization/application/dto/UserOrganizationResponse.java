package br.edu.lms.module.organization.application.dto;

import br.edu.lms.module.organization.domain.model.MemberRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserOrganizationResponse {
    String id;
    String name;
    MemberRole role;
}
