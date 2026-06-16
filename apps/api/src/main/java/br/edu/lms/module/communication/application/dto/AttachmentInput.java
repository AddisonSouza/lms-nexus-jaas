package br.edu.lms.module.communication.application.dto;

import java.io.InputStream;

public record AttachmentInput(
        InputStream stream,
        String fileName,
        String mimeType,
        Long sizeBytes,
        String externalUrl,
        String linkTitle
) {
    public boolean isFile() {
        return stream != null;
    }

    public boolean isLink() {
        return externalUrl != null && !externalUrl.isBlank();
    }
}
