package br.edu.lms.module.communication.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnnouncementAttachmentResponse {
    private String id;
    private String fileKey;
    private String originalName;
    private String mimeType;
    private Long sizeBytes;
    private String externalUrl;
    private String linkTitle;
}
