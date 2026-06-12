package br.edu.lms.module.classroom.interfaces.rest.dto;

import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotBlank String userId,
        @NotNull ClassroomMemberRole role
) {}
