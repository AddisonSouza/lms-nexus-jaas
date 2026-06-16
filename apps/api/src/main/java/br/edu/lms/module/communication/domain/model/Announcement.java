package br.edu.lms.module.communication.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Announcement {

    @EqualsAndHashCode.Include
    private final AnnouncementId id;

    private final String classroomId;
    private final String organizationId;
    private final String authorId;
    private String content;
    private List<AnnouncementAttachment> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public boolean isAuthoredBy(String userId) {
        return this.authorId != null && this.authorId.equals(userId);
    }
}
