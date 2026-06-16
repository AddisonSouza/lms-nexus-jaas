package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.SubmissionResponse;
import br.edu.lms.module.assessment.domain.exception.TaskNotFoundException;
import br.edu.lms.module.assessment.domain.exception.UnauthorizedTaskOperationException;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.port.in.ListTaskSubmissionsUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListTaskSubmissionsService implements ListTaskSubmissionsUseCase {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public List<SubmissionResponse> execute(String taskId, String professorId, String organizationId) {
        var task = taskRepository.findByIdAndOrganization(TaskId.of(taskId), organizationId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (!task.getCreatedBy().equals(professorId)) {
            throw new UnauthorizedTaskOperationException(professorId, taskId);
        }

        return submissionRepository.findByTask(taskId, organizationId)
                .stream()
                .map(SubmitTaskService::toResponse)
                .toList();
    }
}
