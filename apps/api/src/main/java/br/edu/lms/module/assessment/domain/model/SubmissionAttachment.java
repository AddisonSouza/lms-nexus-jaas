package br.edu.lms.module.assessment.domain.model;

public record SubmissionAttachment(
        String id,
        String fileKey,
        String originalName,
        String mimeType,
        long sizeBytes
) {
}
