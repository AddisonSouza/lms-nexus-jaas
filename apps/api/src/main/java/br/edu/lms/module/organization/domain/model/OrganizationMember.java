package br.edu.lms.module.organization.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrganizationMember {

    @EqualsAndHashCode.Include
    private final String id;

    private final String organizationId;
    private final String userId;
    private final MemberRole role;
}
