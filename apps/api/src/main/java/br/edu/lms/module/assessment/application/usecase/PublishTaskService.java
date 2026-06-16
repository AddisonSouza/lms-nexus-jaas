package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.domain.event.TaskCreatedEvent;
import br.edu.lms.module.assessment.domain.exception.InvalidTaskStateException;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.port.in.PublishTaskUseCase;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class PublishTaskService implements PublishTaskUseCase {

    private final TaskRepository taskRepository;
    private final Event<TaskCreatedEvent> taskCreatedEvent;

    @Override
    @Transactional
    public TaskResponse execute(String taskId, String organizationId, String requestingUserId) {
        var task = taskRepository.findByIdAndOrganization(TaskId.of(taskId), organizationId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getCreatedBy().equals(requestingUserId)) {
            throw new UnauthorizedTaskOperationException(requestingUserId, taskId);
        }

        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new InvalidTaskStateException(task.getStatus(), TaskStatus.PUBLISHED);
        }

        var published = task.toBuilder()
                .status(TaskStatus.PUBLISHED)
                .build();

        var saved = taskRepository.save(published);
        taskCreatedEvent.fire(new TaskCreatedEvent(saved.getId().getValue(), saved.getSubjectId(), saved.getOrganizationId()));
        log.info("Task published: {} subject={}", saved.getId().getValue(), saved.getSubjectId());
        return CreateTaskService.toResponse(saved);
    }
}
