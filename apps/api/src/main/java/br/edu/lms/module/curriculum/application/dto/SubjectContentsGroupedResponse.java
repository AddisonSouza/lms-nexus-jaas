package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SubjectContentsGroupedResponse {

    @Value
    @Builder
    public static class TopicWithContents {
        TopicResponse topic;
        List<SubjectContentResponse> contents;
    }

    List<TopicWithContents> topics;
}
