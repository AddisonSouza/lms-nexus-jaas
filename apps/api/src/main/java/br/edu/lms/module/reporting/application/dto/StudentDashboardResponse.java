package br.edu.lms.module.reporting.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class StudentDashboardResponse {
    List<UpcomingTaskResponse> upcomingPendingTasks;
    long pendingTasksCount;
    long submittedTasksCount;
    List<RecentGradeResponse> recentGrades;
    List<SubjectAverageGradeResponse> averageGradePerSubject;
}
