package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SubjectResponse {
    private String id;
    private String name;
    private String code;
    private String description;
    private Integer workloadHours;
    private String organizationId;
    private List<String> classroomIds;
    private List<String> teacherMemberIds;
    private LocalDateTime createdAt;
}
