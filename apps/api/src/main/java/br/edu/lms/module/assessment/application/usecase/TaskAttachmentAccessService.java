package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.domain.model.TaskStatus;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.FileAccessPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * Mesma visibilidade da listagem (`findPublishedByOrganization`): o aluno vê a
 * tarefa publicada ou encerrada, nunca o rascunho; os demais papéis da
 * organização veem todas.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class TaskAttachmentAccessService implements FileAccessPort {

    private static final Set<TaskStatus> VISIBLE_TO_STUDENTS = Set.of(TaskStatus.PUBLISHED, TaskStatus.CLOSED);

    private final TaskRepository taskRepository;

    @Override
    public StorageContext context() {
        return StorageContext.TASK_ATTACHMENT;
    }

    @Override
    public boolean canRead(String fileKey, FileRequester requester) {
        return taskRepository.findByAttachmentFileKey(fileKey, requester.getOrganizationId())
                .map(task -> !"ALUNO".equals(requester.getRole())
                        || VISIBLE_TO_STUDENTS.contains(task.getStatus()))
                .orElse(false);
    }
}
