package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import br.edu.lms.module.storage.domain.model.FileRequester;
import br.edu.lms.module.storage.domain.model.StorageContext;
import br.edu.lms.module.storage.domain.port.out.FileAccessPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

/**
 * Mesmas duas pontas que já leem a submissão: o aluno que a enviou
 * (`GetSubmissionFeedbackService`) e o professor que criou a tarefa
 * (`ListTaskSubmissionsService`).
 */
@ApplicationScoped
@RequiredArgsConstructor
public class SubmissionAttachmentAccessService implements FileAccessPort {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;

    @Override
    public StorageContext context() {
        return StorageContext.SUBMISSION_ATTACHMENT;
    }

    @Override
    public boolean canRead(String fileKey, FileRequester requester) {
        return submissionRepository.findByAttachmentFileKey(fileKey, requester.getOrganizationId())
                .map(submission -> submission.getStudentId().equals(requester.getUserId())
                        || createdTheTask(submission, requester))
                .orElse(false);
    }

    private boolean createdTheTask(TaskSubmission submission, FileRequester requester) {
        return taskRepository.findByIdAndOrganization(TaskId.of(submission.getTaskId()), requester.getOrganizationId())
                .map(task -> task.getCreatedBy().equals(requester.getUserId()))
                .orElse(false);
    }
}
