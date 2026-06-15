package br.edu.lms.module.curriculum.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "subject_topics")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TopicJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "subject_id", nullable = false, length = 36)
    private String subjectId;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "position", nullable = false)
    private int position;

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
