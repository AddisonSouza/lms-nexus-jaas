package br.edu.lms.module.classroom.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "classroom_members")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassroomMemberJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "classroom_id", nullable = false, length = 36)
    private String classroomId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}
