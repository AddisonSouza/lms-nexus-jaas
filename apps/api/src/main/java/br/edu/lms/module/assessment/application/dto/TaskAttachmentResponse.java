package br.edu.lms.module.assessment.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskAttachmentResponse {
    private String id;
    private String fileKey;
    private String originalName;
    private String mimeType;
    private long sizeBytes;
}
