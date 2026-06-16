package br.edu.lms.module.communication.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EditAnnouncementCommand {
    private String announcementId;
    private String userId;
    private String organizationId;
    private String content;
    private List<AttachmentInput> attachments;
}
