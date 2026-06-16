package br.edu.lms.module.assessment.application.dto;

import br.edu.lms.module.assessment.domain.model.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class TaskWithGradeResponse {

    String id;
    String subjectId;
    String organizationId;
    String createdBy;
    String title;
    String description;
    LocalDateTime deadline;
    BigDecimal maxScore;
    TaskStatus status;
    List<TaskAttachmentResponse> attachments;
    LocalDateTime createdAt;

    SubmissionSummary submission;

    @Value
    @Builder
    public static class SubmissionSummary {
        String id;
        String status;
        BigDecimal grade;
        String feedback;
        LocalDateTime submittedAt;
        boolean lateSubmission;
    }
}
