package br.edu.lms.module.organization.application.dto;

import br.edu.lms.module.organization.domain.model.MemberRole;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OrganizationMemberResponse {
    String id;
    String userId;
    String name;
    String email;
    MemberRole role;
    LocalDateTime joinedAt;
    /** O criador da organização não pode ser removido nem ter o papel alterado. */
    boolean owner;
}
