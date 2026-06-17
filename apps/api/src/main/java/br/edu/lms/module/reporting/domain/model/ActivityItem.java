package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ActivityItem {
    ActivityType type;
    String referenceId;
    String description;
    LocalDateTime occurredAt;
}
