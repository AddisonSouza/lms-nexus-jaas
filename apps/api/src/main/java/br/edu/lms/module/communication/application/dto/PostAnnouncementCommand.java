package br.edu.lms.module.communication.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostAnnouncementCommand {
    private String classroomId;
    private String organizationId;
    private String authorId;
    private String content;
    private List<AttachmentInput> attachments;
}
