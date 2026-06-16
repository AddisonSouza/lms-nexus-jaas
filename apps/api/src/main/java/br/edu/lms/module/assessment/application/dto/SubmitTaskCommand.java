package br.edu.lms.module.assessment.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SubmitTaskCommand {
    String taskId;
    String studentId;
    String organizationId;
    String textResponse;
    List<AttachmentInput> attachments;
}
