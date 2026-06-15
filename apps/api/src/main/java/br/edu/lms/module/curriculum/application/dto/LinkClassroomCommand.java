package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkClassroomCommand {
    private String classroomId;
    private String organizationId;
}
