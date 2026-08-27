package br.edu.lms.module.organization.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserOrganization {

    @EqualsAndHashCode.Include
    private final String id;

    private final String name;
    private final MemberRole role;
}
