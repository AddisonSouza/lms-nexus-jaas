package br.edu.lms.module.communication.domain.model;

public record AnnouncementAttachment(
        String id,
        String fileKey,
        String originalName,
        String mimeType,
        Long sizeBytes,
        String externalUrl,
        String linkTitle
) {
}
