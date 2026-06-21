package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface OrganizationMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.organization.domain.model.OrganizationId.of(entity.getId()))")
    Organization toDomain(OrganizationJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    OrganizationJpaEntity toEntity(Organization domain);
}
