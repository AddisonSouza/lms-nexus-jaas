package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class StudentAverageGrade {
    String studentId;
    String studentName;
    BigDecimal averageGrade;
}
