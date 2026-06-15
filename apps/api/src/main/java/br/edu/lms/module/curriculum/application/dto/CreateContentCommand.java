package br.edu.lms.module.curriculum.application.dto;

import br.edu.lms.module.curriculum.domain.model.ContentType;
import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
public class CreateContentCommand {
    String topicId;
    String subjectId;
    String organizationId;
    String title;
    ContentType contentType;
    String externalUrl;
    String description;
    InputStream fileStream;
    String fileName;
    String fileMimeType;
    long fileSizeBytes;
}
