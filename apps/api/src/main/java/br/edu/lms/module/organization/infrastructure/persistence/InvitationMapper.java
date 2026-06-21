package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.ZoneOffset;

@Mapper(componentModel = "cdi", imports = {ZoneOffset.class})
public interface InvitationMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.organization.domain.model.InvitationId.of(entity.getId()))")
    @Mapping(target = "role", expression = "java(br.edu.lms.module.organization.domain.model.MemberRole.valueOf(entity.getRole()))")
    @Mapping(target = "status", expression = "java(br.edu.lms.module.organization.domain.model.InvitationStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "expiresAt", expression = "java(entity.getExpiresAt().toInstant(ZoneOffset.UTC))")
    @Mapping(target = "createdAt", expression = "java(entity.getCreatedAt().toInstant(ZoneOffset.UTC))")
    Invitation toDomain(InvitationJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "role", expression = "java(domain.getRole().name())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "expiresAt", expression = "java(java.time.LocalDateTime.ofInstant(domain.getExpiresAt(), ZoneOffset.UTC))")
    @Mapping(target = "createdAt", expression = "java(domain.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(domain.getCreatedAt(), ZoneOffset.UTC) : null)")
    InvitationJpaEntity toEntity(Invitation domain);
}
