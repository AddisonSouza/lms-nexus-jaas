package br.edu.lms.module.identity.infrastructure.persistence;

import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.FullName;
import br.edu.lms.module.identity.domain.model.User;
import br.edu.lms.module.identity.domain.model.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    @Mapping(target = "id", expression = "java(entity.getId())")
    @Mapping(target = "email", expression = "java(new br.edu.lms.module.identity.domain.model.Email(entity.getEmail()))")
    @Mapping(target = "fullName", expression = "java(new br.edu.lms.module.identity.domain.model.FullName(entity.getFullName()))")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "status", source = "status")
    User toDomain(UserJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "email", expression = "java(domain.getEmail().getValue())")
    @Mapping(target = "fullName", expression = "java(domain.getFullName().getValue())")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    UserJpaEntity toEntity(User domain);
}
