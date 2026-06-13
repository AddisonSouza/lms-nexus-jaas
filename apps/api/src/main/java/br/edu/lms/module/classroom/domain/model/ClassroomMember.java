package br.edu.lms.module.classroom.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassroomMember {

    @EqualsAndHashCode.Include
    private final String id;

    private ClassroomId classroomId;
    private String userId;
    private String organizationId;
    private ClassroomMemberRole role;
    private LocalDateTime joinedAt;
    private LocalDateTime deletedAt;
}
