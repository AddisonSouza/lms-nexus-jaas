package br.edu.lms.module.storage.domain.model;

import lombok.Value;

@Value
public class StoredFile {
    String fileKey;
    String originalName;
    String mimeType;
    long sizeBytes;
}
