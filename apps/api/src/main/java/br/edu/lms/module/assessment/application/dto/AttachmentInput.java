package br.edu.lms.module.assessment.application.dto;

import java.io.InputStream;

public record AttachmentInput(
        InputStream stream,
        String fileName,
        String mimeType,
        long sizeBytes
) {
}
