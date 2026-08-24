package br.edu.lms.module.classroom.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classroom {

    @EqualsAndHashCode.Include
    private final ClassroomId id;

    private String name;
    private String description;
    private String academicPeriod;
    private ClassroomStatus status;

    /** Gerado uma única vez na criação da turma e imutável para sempre (RF-08). */
    private final InviteCode inviteCode;

    private String organizationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
