package br.edu.lms.module.curriculum.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "subject_teachers")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SubjectTeacherJpaEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private SubjectTeacherId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
