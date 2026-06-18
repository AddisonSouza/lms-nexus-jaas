package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AtRiskStudentResponse {
    String studentId;
    String studentName;
    long pendingCount;
}
