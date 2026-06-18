package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class RecentGrade {
    String taskId;
    String title;
    String subjectName;
    BigDecimal grade;
    String feedback;
}
