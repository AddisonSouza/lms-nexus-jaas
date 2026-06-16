package br.edu.lms.module.assessment.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EditSubmissionCommand {
    String submissionId;
    String taskId;
    String studentId;
    String textResponse;
    List<AttachmentInput> attachments;
}
