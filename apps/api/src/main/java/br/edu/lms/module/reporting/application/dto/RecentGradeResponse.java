package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class RecentGradeResponse {
    String taskId;
    String title;
    String subjectName;
    BigDecimal grade;
    String feedback;
}
