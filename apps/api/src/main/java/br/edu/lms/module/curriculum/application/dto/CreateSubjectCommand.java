package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateSubjectCommand {
    private String name;
    private String code;
    private String description;
    private Integer workloadHours;
    private String organizationId;
}
