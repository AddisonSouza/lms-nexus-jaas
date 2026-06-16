package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.TaskResponse;
import br.edu.lms.module.assessment.domain.port.in.ListPublishedTasksUseCase;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListPublishedTasksService implements ListPublishedTasksUseCase {

    private final TaskRepository taskRepository;

    @Override
    public List<TaskResponse> execute(String organizationId) {
        return taskRepository.findPublishedByOrganization(organizationId)
                .stream().map(CreateTaskService::toResponse).toList();
    }
}
