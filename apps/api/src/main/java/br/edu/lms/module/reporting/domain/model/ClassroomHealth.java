package br.edu.lms.module.reporting.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ClassroomHealth {
    String classroomId;
    String classroomName;
    String status;
    BigDecimal deliveryRate;
    BigDecimal averageGrade;
}
