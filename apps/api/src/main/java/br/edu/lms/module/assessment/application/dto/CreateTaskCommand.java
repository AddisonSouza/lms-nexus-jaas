package br.edu.lms.module.assessment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CreateTaskCommand {
    private String subjectId;
    private String organizationId;
    private String createdBy;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private BigDecimal maxScore;
    private List<AttachmentInput> attachments;
}
