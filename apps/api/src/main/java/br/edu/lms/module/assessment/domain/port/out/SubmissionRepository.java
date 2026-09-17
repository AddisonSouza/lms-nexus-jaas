package br.edu.lms.module.assessment.domain.port.out;

import br.edu.lms.module.assessment.domain.model.SubmissionCounts;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SubmissionRepository {
    TaskSubmission save(TaskSubmission submission);
    Optional<TaskSubmission> findById(SubmissionId id);
    Optional<TaskSubmission> findByTaskAndStudent(String taskId, String studentId);
    List<TaskSubmission> findByTask(String taskId, String organizationId);

    List<TaskSubmission> findByStudentAndOrganization(String studentId, String organizationId);

    /**
     * Contagens de uma lista de tarefas numa consulta só. Tarefa sem submissão
     * não aparece no mapa — quem chama decide o que fazer com a ausência.
     */
    Map<String, SubmissionCounts> countByTasks(List<String> taskIds, String organizationId);
}
