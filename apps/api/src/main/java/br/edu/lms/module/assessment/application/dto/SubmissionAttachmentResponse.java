package br.edu.lms.module.assessment.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SubmissionAttachmentResponse {
    String id;
    String fileKey;
    String originalName;
    String mimeType;
    long sizeBytes;
}
