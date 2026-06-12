package br.edu.lms.module.organization.interfaces.rest.dto;

import br.edu.lms.module.organization.domain.model.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @NotBlank @Email String email,
        @NotNull MemberRole role
) {}
