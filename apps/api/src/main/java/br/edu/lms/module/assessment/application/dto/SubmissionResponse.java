package br.edu.lms.module.assessment.application.dto;

import br.edu.lms.module.assessment.domain.model.SubmissionStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class SubmissionResponse {
    String id;
    String taskId;
    String studentId;
    String organizationId;
    String textResponse;
    SubmissionStatus status;
    List<SubmissionAttachmentResponse> attachments;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
