package br.edu.lms.module.organization.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "organization_members")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrganizationMemberJpaEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "CHAR(36)")
    private String organizationId;

    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "role", nullable = false, length = 50)
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
