package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpdateTopicCommand {
    String topicId;
    String subjectId;
    String organizationId;
    String title;
}
