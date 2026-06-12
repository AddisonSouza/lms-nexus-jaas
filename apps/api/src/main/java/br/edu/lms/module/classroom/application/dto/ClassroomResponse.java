package br.edu.lms.module.classroom.application.dto;

import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClassroomResponse {
    private String id;
    private String name;
    private String description;
    private String academicPeriod;
    private ClassroomStatus status;
    private String inviteCode;
    private String organizationId;
    private LocalDateTime createdAt;
}
