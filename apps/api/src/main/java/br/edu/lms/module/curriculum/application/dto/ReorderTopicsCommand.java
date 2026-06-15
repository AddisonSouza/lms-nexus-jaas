package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ReorderTopicsCommand {
    String subjectId;
    String organizationId;
    List<String> topicIds;
}
