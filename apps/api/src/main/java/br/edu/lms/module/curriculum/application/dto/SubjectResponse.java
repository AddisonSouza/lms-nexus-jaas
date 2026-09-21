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
    // Os mesmos professores pelo `userId` do JWT: o front compara com quem está
    // logado para decidir se mostra o painel da disciplina.
    private List<String> teacherUserIds;
    private LocalDateTime createdAt;
}
