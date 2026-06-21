package br.edu.lms.module.identity.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record SwitchOrganizationRequest(
        @NotBlank String organizationId
) {}
