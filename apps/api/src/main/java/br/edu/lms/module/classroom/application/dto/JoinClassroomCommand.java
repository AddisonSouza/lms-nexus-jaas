package br.edu.lms.module.classroom.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinClassroomCommand {
    private String inviteCode;
    private String userId;
    private String organizationId;
}
