package br.edu.lms.module.assessment.application.usecase;

import br.edu.lms.module.assessment.application.dto.TaskAttachmentResponse;
import br.edu.lms.module.assessment.application.dto.TaskWithGradeResponse;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.in.ListStudentGradesUseCase;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class ListStudentGradesService implements ListStudentGradesUseCase {

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    public List<TaskWithGradeResponse> execute(String studentId, String organizationId) {
        List<Task> tasks = taskRepository.findPublishedByOrganization(organizationId);

        Map<String, TaskSubmission> submissionByTaskId = submissionRepository
                .findByStudentAndOrganization(studentId, organizationId)
                .stream()
                .collect(Collectors.toMap(TaskSubmission::getTaskId, Function.identity()));

        return tasks.stream()
                .map(task -> toResponse(task, submissionByTaskId.get(task.getId().getValue())))
                .toList();
    }

    private TaskWithGradeResponse toResponse(Task task, TaskSubmission submission) {
        List<TaskAttachmentResponse> attachments = task.getAttachments() == null ? List.of() :
                task.getAttachments().stream()
                        .map(a -> TaskAttachmentResponse.builder()
                                .id(a.id())
                                .fileKey(a.fileKey())
                                .originalName(a.originalName())
                                .mimeType(a.mimeType())
                                .sizeBytes(a.sizeBytes())
                                .build())
                        .toList();

        TaskWithGradeResponse.SubmissionSummary submissionSummary = null;
        if (submission != null) {
            boolean late = submission.getCreatedAt() != null
                    && task.getDeadline() != null
                    && submission.getCreatedAt().isAfter(task.getDeadline());

            submissionSummary = TaskWithGradeResponse.SubmissionSummary.builder()
                    .id(submission.getId().getValue())
                    .status(submission.getStatus().name())
                    .grade(submission.getGrade())
                    .feedback(submission.getFeedback())
                    .submittedAt(submission.getCreatedAt())
                    .lateSubmission(late)
                    .build();
        }

        return TaskWithGradeResponse.builder()
                .id(task.getId().getValue())
                .subjectId(task.getSubjectId())
                .organizationId(task.getOrganizationId())
                .createdBy(task.getCreatedBy())
                .title(task.getTitle())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .maxScore(task.getMaxScore())
                .status(task.getStatus())
                .attachments(attachments)
                .createdAt(task.getCreatedAt())
                .submission(submissionSummary)
                .build();
    }
}
