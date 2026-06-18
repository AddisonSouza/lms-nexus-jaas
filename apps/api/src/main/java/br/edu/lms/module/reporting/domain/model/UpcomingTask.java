package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class UpcomingTask {
    String taskId;
    String title;
    String subjectName;
    LocalDateTime deadline;
}
