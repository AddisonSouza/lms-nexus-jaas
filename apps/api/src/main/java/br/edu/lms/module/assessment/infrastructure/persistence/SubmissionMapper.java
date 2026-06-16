package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.SubmissionAttachment;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface SubmissionMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.assessment.domain.model.SubmissionId.of(entity.getId()))")
    @Mapping(target = "status", expression = "java(br.edu.lms.module.assessment.domain.model.SubmissionStatus.valueOf(entity.getStatus()))")
    @Mapping(target = "deletedAt", source = "deletedAt")
    TaskSubmission toDomain(TaskSubmissionJpaEntity entity);

    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    TaskSubmissionJpaEntity toEntity(TaskSubmission domain);

    default SubmissionAttachment toAttachmentDomain(SubmissionAttachmentJpaEntity entity) {
        return new SubmissionAttachment(
                entity.getId(),
                entity.getFileKey(),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSizeBytes()
        );
    }
}
