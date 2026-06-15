package br.edu.lms.module.curriculum.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LinkClassroomRequest(
        @NotBlank(message = "classroomId is required")
        String classroomId
) {}
