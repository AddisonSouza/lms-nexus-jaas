package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ClassroomHealthResponse {
    String classroomId;
    String classroomName;
    String status;
    BigDecimal deliveryRate;
    BigDecimal averageGrade;
    List<AtRiskStudentResponse> atRiskStudents;
}
