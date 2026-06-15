package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class TopicResponse {
    String id;
    String subjectId;
    String organizationId;
    String title;
    int position;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
