package br.edu.lms.module.assessment.application.dto;

import br.edu.lms.module.assessment.domain.model.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TaskResponse {
    private String id;
    private String subjectId;
    private String organizationId;
    private String createdBy;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal maxScore;
    private TaskStatus status;
    private List<TaskAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
