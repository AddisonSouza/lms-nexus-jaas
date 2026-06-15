package br.edu.lms.module.curriculum.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubjectContent {

    @EqualsAndHashCode.Include
    private final SubjectContentId id;

    private String topicId;
    private String organizationId;
    private String title;
    private ContentType contentType;
    private String externalUrl;
    private String fileKey;
    private String description;
    private int position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
