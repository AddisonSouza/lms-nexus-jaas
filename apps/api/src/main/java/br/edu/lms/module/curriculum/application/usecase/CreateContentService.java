package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.CreateContentCommand;
import br.edu.lms.module.curriculum.application.dto.SubjectContentResponse;
import br.edu.lms.module.curriculum.domain.exception.InvalidFileTypeException;
import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.curriculum.domain.model.ContentType;
import br.edu.lms.module.curriculum.domain.model.SubjectContent;
import br.edu.lms.module.curriculum.domain.model.SubjectContentId;
import br.edu.lms.module.curriculum.domain.port.in.CreateContentUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateContentService implements CreateContentUseCase {

    private static final Set<String> ALLOWED_LESSON_MIME_TYPES = Set.of(
            "application/pdf",
            "video/mp4",
            "video/webm",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip",
            "image/jpeg",
            "image/png"
    );

    private final ContentRepository contentRepository;
    private final TopicRepository topicRepository;
    private final StoragePort storagePort;

    @Override
    public SubjectContentResponse execute(CreateContentCommand command) {
        topicRepository.findById(command.getTopicId(), command.getOrganizationId())
                .orElseThrow(() -> new TopicNotFoundException(command.getTopicId()));

        String fileKey = null;
        if (command.getFileStream() != null) {
            validateMimeType(command.getFileMimeType());
            var stored = storagePort.store(
                    command.getFileStream(),
                    command.getFileName(),
                    command.getFileMimeType(),
                    command.getFileSizeBytes(),
                    StorageContext.LESSON_MATERIAL);
            fileKey = stored.getFileKey();
        }

        int nextPosition = contentRepository.maxPositionByTopicId(command.getTopicId(), command.getOrganizationId()) + 1;

        var content = SubjectContent.builder()
                .id(SubjectContentId.generate())
                .topicId(command.getTopicId())
                .organizationId(command.getOrganizationId())
                .title(command.getTitle())
                .contentType(command.getContentType())
                .externalUrl(command.getExternalUrl())
                .fileKey(fileKey)
                .description(command.getDescription())
                .position(nextPosition)
                .build();

        contentRepository.save(content);
        log.info("Content created: {} type={} topic={}", content.getId().getValue(), command.getContentType(), command.getTopicId());

        return toResponse(content);
    }

    private void validateMimeType(String mimeType) {
        if (mimeType == null || !ALLOWED_LESSON_MIME_TYPES.contains(mimeType)) {
            throw new InvalidFileTypeException(mimeType);
        }
    }

    static SubjectContentResponse toResponse(SubjectContent c) {
        return SubjectContentResponse.builder()
                .id(c.getId().getValue())
                .topicId(c.getTopicId())
                .organizationId(c.getOrganizationId())
                .title(c.getTitle())
                .contentType(c.getContentType())
                .externalUrl(c.getExternalUrl())
                .fileKey(c.getFileKey())
                .description(c.getDescription())
                .position(c.getPosition())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
