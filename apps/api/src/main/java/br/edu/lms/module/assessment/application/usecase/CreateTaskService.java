package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.AttachmentInput;
import br.edu.lms.module.assessment.application.dto.CreateTaskCommand;
import br.edu.lms.module.assessment.application.dto.TaskAttachmentResponse;
import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskAttachment;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.port.in.CreateTaskUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubjectQueryPort;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import br.edu.lms.module.curriculum.domain.exception.InvalidFileTypeException;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateTaskService implements CreateTaskUseCase {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip",
            "image/jpeg",
            "image/png"
    );

    private final TaskRepository taskRepository;
    private final SubjectQueryPort subjectQueryPort;
    private final StoragePort storagePort;

    @Override
    public TaskResponse execute(CreateTaskCommand command) {
        if (!subjectQueryPort.existsByIdAndTeacher(command.getSubjectId(), command.getOrganizationId(), command.getCreatedBy())) {
            throw new UnauthorizedTaskOperationException(command.getCreatedBy(), command.getSubjectId());
        }

        if (!command.getDeadline().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Task deadline must be in the future");
        }

        List<TaskAttachment> attachments = new ArrayList<>();
        if (command.getAttachments() != null) {
            for (AttachmentInput input : command.getAttachments()) {
                validateMimeType(input.mimeType());
                var stored = storagePort.store(
                        input.stream(),
                        input.fileName(),
                        input.mimeType(),
                        input.sizeBytes(),
                        StorageContext.TASK_ATTACHMENT);
                attachments.add(new TaskAttachment(
                        UUID.randomUUID().toString(),
                        stored.getFileKey(),
                        input.fileName(),
                        input.mimeType(),
                        input.sizeBytes()));
            }
        }

        var task = Task.builder()
                .id(TaskId.generate())
                .subjectId(command.getSubjectId())
                .organizationId(command.getOrganizationId())
                .createdBy(command.getCreatedBy())
                .title(command.getTitle())
                .description(command.getDescription())
                .deadline(command.getDeadline())
                .maxScore(command.getMaxScore())
                .status(TaskStatus.DRAFT)
                .attachments(attachments)
                .build();

        var saved = taskRepository.save(task);
        log.info("Task created: {} subject={} org={}", saved.getId().getValue(), saved.getSubjectId(), saved.getOrganizationId());
        return toResponse(saved);
    }

    private void validateMimeType(String mimeType) {
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new InvalidFileTypeException(mimeType);
        }
    }

    static TaskResponse toResponse(Task task) {
        List<TaskAttachmentResponse> attachmentResponses = task.getAttachments() == null ? List.of() :
                task.getAttachments().stream().map(a -> TaskAttachmentResponse.builder()
                        .id(a.id())
                        .fileKey(a.fileKey())
                        .originalName(a.originalName())
                        .mimeType(a.mimeType())
                        .sizeBytes(a.sizeBytes())
                        .build()).toList();

        return TaskResponse.builder()
                .id(task.getId().getValue())
                .subjectId(task.getSubjectId())
                .organizationId(task.getOrganizationId())
                .createdBy(task.getCreatedBy())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .maxScore(task.getMaxScore())
                .status(task.getStatus())
                .attachments(attachmentResponses)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
