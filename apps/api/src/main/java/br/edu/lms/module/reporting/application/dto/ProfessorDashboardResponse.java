package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ProfessorDashboardResponse {
    long pendingEvaluationsCount;
    List<BigDecimal> lastTaskGradeDistribution;
    List<StudentSummaryResponse> studentsWithoutSubmission;
    List<StudentAverageGradeResponse> averageGradePerStudent;
}
