package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.AttachmentInput;
import br.edu.lms.module.assessment.application.dto.EditSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.domain.event.TaskSubmittedEvent;
import br.edu.lms.module.assessment.domain.exception.DeadlineExpiredException;
import br.edu.lms.module.assessment.domain.exception.SubmissionAlreadyEvaluatedException;
import br.edu.lms.module.assessment.domain.exception.SubmissionNotFoundException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.SubmissionAttachment;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.SubmissionStatus;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.in.EditSubmissionUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
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
public class EditSubmissionService implements EditSubmissionUseCase {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final StoragePort storagePort;
    private final Event<TaskSubmittedEvent> submittedEvent;

    @Override
    public SubmissionResponse execute(EditSubmissionCommand command) {
        var submission = submissionRepository.findById(SubmissionId.of(command.getSubmissionId()))
                .orElseThrow(() -> new SubmissionNotFoundException(command.getSubmissionId()));

        if (!submission.getStudentId().equals(command.getStudentId())) {
            throw new UnauthorizedTaskOperationException(command.getStudentId(), command.getSubmissionId());
        }

        if (submission.getStatus() == SubmissionStatus.EVALUATED) {
            throw new SubmissionAlreadyEvaluatedException(command.getSubmissionId());
        }

        var task = taskRepository.findById(TaskId.of(submission.getTaskId()))
                .orElseThrow(() -> new TaskNotFoundException(submission.getTaskId()));

        if (!LocalDateTime.now().isBefore(task.getDeadline())) {
            throw new DeadlineExpiredException(submission.getTaskId());
        }

        boolean hasText = command.getTextResponse() != null && !command.getTextResponse().isBlank();
        boolean hasFiles = command.getAttachments() != null && !command.getAttachments().isEmpty();
        if (!hasText && !hasFiles) {
            throw new IllegalArgumentException("Submission must have text or at least one file");
        }

        List<SubmissionAttachment> attachments = buildAttachments(command.getAttachments());

        var updated = submission.toBuilder()
                .textResponse(command.getTextResponse())
                .attachments(attachments.isEmpty() ? submission.getAttachments() : attachments)
                .build();

        var saved = submissionRepository.save(updated);
        submittedEvent.fire(new TaskSubmittedEvent(saved.getId().getValue(), saved.getTaskId(), saved.getStudentId(), saved.getOrganizationId()));
        log.info("Submission edited: submission={} student={}", saved.getId().getValue(), saved.getStudentId());
        return SubmitTaskService.toResponse(saved);
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
}
