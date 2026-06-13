package br.edu.lms.module.organization.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invitations")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InvitationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "CHAR(36)")
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "CHAR(36)")
    private String organizationId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private String token;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "invited_by", nullable = false, columnDefinition = "CHAR(36)")
    private String invitedBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
