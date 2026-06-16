package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.AnnouncementAttachmentResponse;
import br.edu.lms.module.communication.application.dto.AttachmentInput;
import br.edu.lms.module.communication.application.dto.AnnouncementResponse;
import br.edu.lms.module.communication.application.dto.PostAnnouncementCommand;
import br.edu.lms.module.communication.domain.event.AnnouncementPostedEvent;
import br.edu.lms.module.communication.domain.exception.EmptyContentException;
import br.edu.lms.module.communication.domain.exception.InvalidAttachmentTypeException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.model.Announcement;
import br.edu.lms.module.communication.domain.model.AnnouncementAttachment;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.in.PostAnnouncementUseCase;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class PostAnnouncementService implements PostAnnouncementUseCase {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip", "image/jpeg", "image/png");

    private final AnnouncementRepository announcementRepository;
    private final ClassroomQueryPort classroomQueryPort;
    private final StoragePort storagePort;
    private final Event<AnnouncementPostedEvent> postedEvent;

    @Override
    public AnnouncementResponse execute(PostAnnouncementCommand command) {
        if (!classroomQueryPort.isMember(command.getAuthorId(), command.getClassroomId(), command.getOrganizationId(), "PROFESSOR")) {
            throw new UnauthorizedAnnouncementOperationException(command.getAuthorId(), command.getClassroomId());
        }

        if (command.getContent() == null || command.getContent().isBlank()) {
            throw new EmptyContentException();
        }

        List<AnnouncementAttachment> attachments = buildAttachments(command.getAttachments());

        var announcement = Announcement.builder()
                .id(AnnouncementId.generate())
                .classroomId(command.getClassroomId())
                .organizationId(command.getOrganizationId())
                .authorId(command.getAuthorId())
                .content(command.getContent())
                .attachments(attachments)
                .build();

        var saved = announcementRepository.save(announcement);
        postedEvent.fire(new AnnouncementPostedEvent(
                saved.getId().getValue(), saved.getClassroomId(), saved.getAuthorId(), saved.getOrganizationId()));
        log.info("Announcement posted: id={} classroom={} author={}", saved.getId().getValue(), saved.getClassroomId(), saved.getAuthorId());
        return toResponse(saved);
    }

    private List<AnnouncementAttachment> buildAttachments(List<AttachmentInput> inputs) {
        return buildAttachments(inputs, storagePort);
    }

    static List<AnnouncementAttachment> buildAttachments(List<AttachmentInput> inputs, StoragePort storagePort) {
        if (inputs == null) return List.of();
        List<AnnouncementAttachment> result = new ArrayList<>();
        for (AttachmentInput input : inputs) {
            if (input.isFile()) {
                if (!ALLOWED_MIME_TYPES.contains(input.mimeType())) {
                    throw new InvalidAttachmentTypeException(input.mimeType());
                }
                var stored = storagePort.store(input.stream(), input.fileName(), input.mimeType(), input.sizeBytes(), StorageContext.ANNOUNCEMENT_ATTACHMENT);
                result.add(new AnnouncementAttachment(
                        UUID.randomUUID().toString(), stored.getFileKey(), input.fileName(), input.mimeType(), input.sizeBytes(), null, null));
            } else if (input.isLink()) {
                result.add(new AnnouncementAttachment(
                        UUID.randomUUID().toString(), null, null, null, null, input.externalUrl(), input.linkTitle()));
            }
        }
        return result;
    }

    public static AnnouncementResponse toResponse(Announcement a) {
        List<AnnouncementAttachmentResponse> attachmentResponses = a.getAttachments() == null ? List.of() :
                a.getAttachments().stream().map(att -> AnnouncementAttachmentResponse.builder()
                        .id(att.id())
                        .fileKey(att.fileKey())
                        .originalName(att.originalName())
                        .mimeType(att.mimeType())
                        .sizeBytes(att.sizeBytes())
                        .externalUrl(att.externalUrl())
                        .linkTitle(att.linkTitle())
                        .build()).toList();
        return AnnouncementResponse.builder()
                .id(a.getId().getValue())
                .classroomId(a.getClassroomId())
                .organizationId(a.getOrganizationId())
                .authorId(a.getAuthorId())
                .content(a.getContent())
                .attachments(attachmentResponses)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
