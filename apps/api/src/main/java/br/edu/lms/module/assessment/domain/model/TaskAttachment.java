package br.edu.lms.module.assessment.domain.model;

public record TaskAttachment(
        String id,
        String fileKey,
        String originalName,
        String mimeType,
        long sizeBytes
) {
}
