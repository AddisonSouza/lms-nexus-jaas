package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ActivityItemResponse {
    String type;
    String referenceId;
    String description;
    LocalDateTime occurredAt;
}
