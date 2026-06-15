package br.edu.lms.module.curriculum.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignTeacherCommand {
    private String memberId;
    private String organizationId;
}
