package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class AdminDashboardResponse {
    LocalDate from;
    LocalDate to;
    Map<String, Long> classroomsByStatus;
    Map<String, Long> membersByRole;
    long tasksCreated;
    long tasksEvaluated;
    BigDecimal averageDeliveryRate;
    List<ActivityItemResponse> activity;
}
