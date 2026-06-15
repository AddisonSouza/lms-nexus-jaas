package br.edu.lms.module.curriculum.application.dto;

import br.edu.lms.module.curriculum.domain.model.ContentType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class SubjectContentResponse {
    String id;
    String topicId;
    String organizationId;
    String title;
    ContentType contentType;
    String externalUrl;
    String fileKey;
    String description;
    int position;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
