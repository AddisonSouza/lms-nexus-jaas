package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateContentCommand {
    String contentId;
    String subjectId;
    String organizationId;
    String title;
    String externalUrl;
    String description;
}
