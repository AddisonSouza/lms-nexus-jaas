package br.edu.lms.module.assessment.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TaskSubmission {

    @EqualsAndHashCode.Include
    private final SubmissionId id;

    private final String taskId;
    private final String studentId;
    private final String organizationId;
    private String textResponse;
    private SubmissionStatus status;
    private java.math.BigDecimal grade;
    private String feedback;
    private List<SubmissionAttachment> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
