package br.edu.lms.module.communication.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private String id;
    private String type;
    private String referenceId;
    private String title;
    private String message;
    private String actionLink;
    private boolean read;
    private LocalDateTime createdAt;
}
