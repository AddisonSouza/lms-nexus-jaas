package br.edu.lms.module.curriculum.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Subject {

    @EqualsAndHashCode.Include
    private final SubjectId id;

    private String name;
    private SubjectCode code;
    private String description;
    private Integer workloadHours;
    private String organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
