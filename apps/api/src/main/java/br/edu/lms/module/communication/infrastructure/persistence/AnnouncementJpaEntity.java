package br.edu.lms.module.communication.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "announcements")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnnouncementJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "classroom_id", nullable = false, length = 36)
    private String classroomId;

    @Column(name = "organization_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "author_id", nullable = false, length = 36)
    private String authorId;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnnouncementAttachmentJpaEntity> attachments;

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
