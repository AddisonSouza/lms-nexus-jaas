package br.edu.lms.module.classroom.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinClassroomRequest(
        @NotBlank @Size(min = 6, max = 6) String inviteCode
) {}
