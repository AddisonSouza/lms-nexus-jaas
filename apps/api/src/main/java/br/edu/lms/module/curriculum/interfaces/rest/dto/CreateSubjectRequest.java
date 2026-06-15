package br.edu.lms.module.curriculum.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubjectRequest(
        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must be at most 255 characters")
        String name,

        @Size(max = 20, message = "code must be at most 20 characters")
        String code,

        String description,

        Integer workloadHours
) {}
