package br.edu.lms.module.assessment.domain.port.out;

import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskId;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(TaskId id);
    Optional<Task> findByIdAndOrganization(TaskId id, String organizationId);
    List<Task> findByOrganizationAndCreatedBy(String organizationId, String createdBy);
    List<Task> findPublishedByOrganization(String organizationId);
    /** Tarefa ativa da organização que tem este arquivo entre os anexos. */
    Optional<Task> findByAttachmentFileKey(String fileKey, String organizationId);
}
