package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.application.dto.TaskAttachmentResponse;
import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskAttachment;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface TaskMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.assessment.domain.model.TaskId.of(entity.getId()))")
    @Mapping(target = "status", expression = "java(br.edu.lms.module.assessment.domain.model.TaskStatus.valueOf(entity.getStatus()))")
    Task toDomain(TaskJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    TaskJpaEntity toEntity(Task domain);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fileKey", source = "fileKey")
    @Mapping(target = "originalName", source = "originalName")
    @Mapping(target = "mimeType", source = "mimeType")
    @Mapping(target = "sizeBytes", source = "sizeBytes")
    TaskAttachmentResponse toAttachmentResponse(TaskAttachment attachment);
}
