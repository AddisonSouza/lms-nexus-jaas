package br.edu.lms.module.assessment.application.mapper;

import br.edu.lms.module.assessment.application.dto.TaskAttachmentResponse;
import br.edu.lms.module.assessment.application.dto.TaskSummaryResponse;
import br.edu.lms.module.assessment.domain.model.SubmissionCounts;
import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface TaskSummaryMapper {

    @Mapping(target = "id", expression = "java(task.getId().getValue())")
    @Mapping(target = "status", expression = "java(task.effectiveStatus())")
    @Mapping(target = "attachments", source = "task.attachments")
    @Mapping(target = "submissionCount", source = "counts.total")
    @Mapping(target = "pendingEvaluationCount", source = "counts.pendingEvaluation")
    TaskSummaryResponse toResponse(Task task, SubmissionCounts counts);

    TaskAttachmentResponse toAttachmentResponse(TaskAttachment attachment);
}
