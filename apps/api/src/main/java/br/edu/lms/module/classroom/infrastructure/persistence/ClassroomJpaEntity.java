package br.edu.lms.module.classroom.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "classrooms")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassroomJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "academic_period", nullable = false, length = 100)
    private String academicPeriod;

    @Column(name = "status", nullable = false, columnDefinition = "ENUM('ACTIVE','ARCHIVED')")
    private String status;

    @Column(name = "invite_code", nullable = false, length = 6)
    private String inviteCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
