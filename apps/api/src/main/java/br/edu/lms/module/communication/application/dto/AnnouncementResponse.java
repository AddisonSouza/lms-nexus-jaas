package br.edu.lms.module.communication.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AnnouncementResponse {
    private String id;
    private String classroomId;
    private String organizationId;
    private String authorId;
    private String content;
    private List<AnnouncementAttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
