package br.edu.lms.module.classroom.application.dto;

import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateClassroomCommand {
    private String name;
    private String description;
    private String academicPeriod;
    private ClassroomStatus status;
    private String organizationId;
}
