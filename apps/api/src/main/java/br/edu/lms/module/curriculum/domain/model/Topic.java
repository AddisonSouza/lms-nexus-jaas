package br.edu.lms.module.curriculum.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Topic {

    @EqualsAndHashCode.Include
    private final TopicId id;

    private String subjectId;
    private String organizationId;
    private String title;
    private int position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
