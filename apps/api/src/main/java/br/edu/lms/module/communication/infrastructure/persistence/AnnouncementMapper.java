package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.application.dto.AnnouncementAttachmentResponse;
import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface AnnouncementMapper {

    @Mapping(target = "id", expression = "java(br.edu.lms.module.communication.domain.model.AnnouncementId.of(entity.getId()))")
    Announcement toDomain(AnnouncementJpaEntity entity);

    // deletedAt is intentionally NOT ignored (unlike createdAt/updatedAt): there is no
    // @PrePersist/@PreUpdate callback that sets it, so soft-delete relies on it being
    // carried through from the domain object on every merge().
    @Mapping(target = "id", expression = "java(domain.getId().getValue())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    AnnouncementJpaEntity toEntity(Announcement domain);

    AnnouncementAttachmentResponse toAttachmentResponse(AnnouncementAttachment attachment);

    default AnnouncementAttachment toAttachmentDomain(AnnouncementAttachmentJpaEntity entity) {
        return new AnnouncementAttachment(
                entity.getId(),
                entity.getFileKey(),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSizeBytes(),
                entity.getExternalUrl(),
                entity.getLinkTitle()
        );
    }
}
