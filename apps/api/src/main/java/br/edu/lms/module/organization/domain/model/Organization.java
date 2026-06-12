package br.edu.lms.module.organization.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Organization {

    @EqualsAndHashCode.Include
    private final OrganizationId id;

    private final String name;
    private final String description;
    private final String ownerId;
}
