package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StudentSummaryResponse {
    String studentId;
    String studentName;
}
