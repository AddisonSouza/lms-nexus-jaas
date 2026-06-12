package br.edu.lms.module.classroom.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateClassroomCommand {
    private String name;
    private String description;
    private String academicPeriod;
    private String organizationId;
}
