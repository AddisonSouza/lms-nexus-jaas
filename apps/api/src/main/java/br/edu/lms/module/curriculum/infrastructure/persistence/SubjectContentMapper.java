package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.application.dto.SubjectContentResponse;
import br.edu.lms.module.curriculum.domain.model.ContentType;
import br.edu.lms.module.curriculum.domain.model.SubjectContent;
import br.edu.lms.module.curriculum.domain.model.SubjectContentId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi", imports = {SubjectContentId.class, ContentType.class})
public interface SubjectContentMapper {

    @Mapping(target = "id", expression = "java(SubjectContentId.of(entity.getId()))")
    @Mapping(target = "contentType", expression = "java(ContentType.valueOf(entity.getContentType()))")
    SubjectContent toDomain(SubjectContentJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "contentType", expression = "java(domain.getContentType().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    SubjectContentJpaEntity toEntity(SubjectContent domain);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    SubjectContentResponse toResponse(SubjectContent domain);
}
