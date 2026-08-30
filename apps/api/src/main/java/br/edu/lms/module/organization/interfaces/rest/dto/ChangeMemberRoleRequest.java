package br.edu.lms.module.organization.interfaces.rest.dto;

import br.edu.lms.module.organization.domain.model.MemberRole;
import jakarta.validation.constraints.NotNull;

public record ChangeMemberRoleRequest(@NotNull MemberRole role) {}
