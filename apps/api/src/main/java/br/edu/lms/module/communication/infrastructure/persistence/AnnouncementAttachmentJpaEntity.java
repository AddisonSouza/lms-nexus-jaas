package br.edu.lms.module.communication.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "announcement_attachments")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AnnouncementAttachmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    @EqualsAndHashCode.Include
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private AnnouncementJpaEntity announcement;

    @Column(name = "file_key", length = 512)
    private String fileKey;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "mime_type", length = 127)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "external_url", columnDefinition = "TEXT")
    private String externalUrl;

    @Column(name = "link_title")
    private String linkTitle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
