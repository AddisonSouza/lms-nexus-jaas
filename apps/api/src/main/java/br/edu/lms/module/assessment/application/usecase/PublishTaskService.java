package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.domain.event.TaskPublishedEvent;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.TaskId;
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
    private final Event<TaskPublishedEvent> taskPublishedEvent;

    @Override
    @Transactional
    public TaskResponse execute(String taskId, String organizationId, String requestingUserId) {
        var task = taskRepository.findByIdAndOrganization(TaskId.of(taskId), organizationId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getCreatedBy().equals(requestingUserId)) {
            throw new UnauthorizedTaskOperationException(requestingUserId, taskId);
        }

        var published = task.publish();

        var saved = taskRepository.save(published);
        taskPublishedEvent.fire(new TaskPublishedEvent(saved.getId().getValue(), saved.getSubjectId(), saved.getOrganizationId()));
        log.info("Task published: {} subject={}", saved.getId().getValue(), saved.getSubjectId());
        return CreateTaskService.toResponse(saved);
    }
}
