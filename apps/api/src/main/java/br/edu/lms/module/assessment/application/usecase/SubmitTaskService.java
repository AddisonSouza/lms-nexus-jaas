package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.AttachmentInput;
import br.edu.lms.module.assessment.application.dto.SubmissionAttachmentResponse;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.application.dto.SubmitTaskCommand;
import br.edu.lms.module.assessment.domain.event.TaskSubmittedEvent;
import br.edu.lms.module.assessment.domain.exception.DeadlineExpiredException;
import br.edu.lms.module.assessment.domain.exception.EmptySubmissionException;
import br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyExistsException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.model.SubmissionAttachment;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.SubmissionStatus;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.in.SubmitTaskUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class SubmitTaskService implements SubmitTaskUseCase {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final StoragePort storagePort;
    private final Event<TaskSubmittedEvent> submittedEvent;

    @Override
    public SubmissionResponse execute(SubmitTaskCommand command) {
        var task = taskRepository.findByIdAndOrganization(TaskId.of(command.getTaskId()), command.getOrganizationId())
                .orElseThrow(() -> new TaskNotFoundException(command.getTaskId()));

        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new InvalidTaskStateException(task.getStatus(), TaskStatus.PUBLISHED);
        }

        if (!LocalDateTime.now().isBefore(task.getDeadline())) {
            throw new DeadlineExpiredException(command.getTaskId());
        }

        boolean hasText = command.getTextResponse() != null && !command.getTextResponse().isBlank();
        boolean hasFiles = command.getAttachments() != null && !command.getAttachments().isEmpty();
        if (!hasText && !hasFiles) {
            throw new EmptySubmissionException();
        }

        submissionRepository.findByTaskAndStudent(command.getTaskId(), command.getStudentId())
                .ifPresent(s -> { throw new SubmissionAlreadyExistsException(command.getTaskId(), command.getStudentId()); });

        List<SubmissionAttachment> attachments = buildAttachments(command.getAttachments());

        var submission = TaskSubmission.builder()
                .id(SubmissionId.generate())
                .taskId(command.getTaskId())
                .studentId(command.getStudentId())
                .organizationId(command.getOrganizationId())
                .textResponse(command.getTextResponse())
                .status(SubmissionStatus.SUBMITTED)
                .attachments(attachments)
                .build();

        var saved = submissionRepository.save(submission);
        submittedEvent.fire(new TaskSubmittedEvent(saved.getId().getValue(), saved.getTaskId(), saved.getStudentId(), saved.getOrganizationId()));
        log.info("Task submitted: submission={} task={} student={}", saved.getId().getValue(), saved.getTaskId(), saved.getStudentId());
        return toResponse(saved);
    }

    private List<SubmissionAttachment> buildAttachments(List<AttachmentInput> inputs) {
        if (inputs == null) return List.of();
        List<SubmissionAttachment> result = new ArrayList<>();
        for (AttachmentInput input : inputs) {
            var stored = storagePort.store(input.stream(), input.fileName(), input.mimeType(), input.sizeBytes(), StorageContext.SUBMISSION_ATTACHMENT);
            result.add(new SubmissionAttachment(UUID.randomUUID().toString(), stored.getFileKey(), input.fileName(), input.mimeType(), input.sizeBytes()));
        }
        return result;
    }

    public static SubmissionResponse toResponse(TaskSubmission s) {
        List<SubmissionAttachmentResponse> attachmentResponses = s.getAttachments() == null ? List.of() :
                s.getAttachments().stream().map(a -> SubmissionAttachmentResponse.builder()
                        .id(a.id())
                        .fileKey(a.fileKey())
                        .originalName(a.originalName())
                        .mimeType(a.mimeType())
                        .sizeBytes(a.sizeBytes())
                        .build()).toList();
        return SubmissionResponse.builder()
                .id(s.getId().getValue())
                .taskId(s.getTaskId())
                .studentId(s.getStudentId())
                .organizationId(s.getOrganizationId())
                .textResponse(s.getTextResponse())
                .status(s.getStatus())
                .grade(s.getGrade())
                .feedback(s.getFeedback())
                .attachments(attachmentResponses)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
