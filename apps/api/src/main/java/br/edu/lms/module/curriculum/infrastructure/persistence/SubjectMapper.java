package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectCode;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface SubjectMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.curriculum.domain.model.SubjectId.of(entity.getId()))")
    @Mapping(target = "code", expression = "java(br.edu.lms.module.curriculum.domain.model.SubjectCode.of(entity.getCode()))")
    Subject toDomain(SubjectJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "code", expression = "java(domain.getCode() != null ? domain.getCode().getValue() : null)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    SubjectJpaEntity toEntity(Subject domain);
}
