package br.edu.lms.module.storage.domain.model;

import java.util.Arrays;
import java.util.Optional;

public enum StorageContext {
    LESSON_MATERIAL,
    TASK_ATTACHMENT,
    SUBMISSION_ATTACHMENT,
    ANNOUNCEMENT_ATTACHMENT;

    /** A chave começa pelo contexto em minúsculas (`task_attachment/2026/09/...`), como o `S3StorageAdapter` a monta. */
    public static Optional<StorageContext> fromFileKey(String fileKey) {
        var prefix = fileKey.split("/", 2)[0];
        return Arrays.stream(values())
                .filter(context -> context.name().toLowerCase().equals(prefix))
                .findFirst();
    }
}
