package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.domain.model.Topic;
import br.edu.lms.module.curriculum.domain.model.TopicId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi", imports = {TopicId.class})
public interface TopicMapper {

    @Mapping(target = "id", expression = "java(TopicId.of(entity.getId()))")
    Topic toDomain(TopicJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TopicJpaEntity toEntity(Topic domain);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    TopicResponse toResponse(Topic domain);
}
