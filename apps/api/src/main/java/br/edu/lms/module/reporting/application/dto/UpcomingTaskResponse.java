package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class UpcomingTaskResponse {
    String taskId;
    String title;
    String subjectName;
    LocalDateTime deadline;
}
