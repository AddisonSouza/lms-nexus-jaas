package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class SubjectAverageGrade {
    String subjectId;
    String subjectName;
    BigDecimal averageGrade;
}
