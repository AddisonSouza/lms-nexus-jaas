package br.edu.lms.module.curriculum.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTeacherRequest(
        @NotBlank(message = "memberId is required")
        String memberId
) {}
