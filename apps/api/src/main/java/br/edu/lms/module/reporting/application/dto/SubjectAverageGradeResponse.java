package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class SubjectAverageGradeResponse {
    String subjectId;
    String subjectName;
    BigDecimal averageGrade;
}
