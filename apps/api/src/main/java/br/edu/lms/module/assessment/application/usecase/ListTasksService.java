package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.TaskSummaryResponse;
import br.edu.lms.module.assessment.application.mapper.TaskSummaryMapper;
import br.edu.lms.module.assessment.domain.model.SubmissionCounts;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.port.in.ListTasksUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class ListTasksService implements ListTasksUseCase {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final TaskSummaryMapper taskSummaryMapper;

    @Override
    public List<TaskSummaryResponse> execute(String organizationId, String professorId) {
        List<Task> tasks = taskRepository.findByOrganizationAndCreatedBy(organizationId, professorId);

        // Uma consulta para a lista inteira, em vez de uma por tarefa.
        List<String> taskIds = tasks.stream().map(t -> t.getId().getValue()).toList();
        Map<String, SubmissionCounts> countsByTask = submissionRepository.countByTasks(taskIds, organizationId);

        return tasks.stream()
                .map(task -> taskSummaryMapper.toResponse(
                        task,
                        // Tarefa sem submissão não vem na agregação: zera.
                        countsByTask.getOrDefault(task.getId().getValue(), SubmissionCounts.none())))
                .toList();
    }
}
