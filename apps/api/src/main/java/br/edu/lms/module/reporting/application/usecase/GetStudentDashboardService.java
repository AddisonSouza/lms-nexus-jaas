package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.application.dto.RecentGradeResponse;
import br.edu.lms.module.reporting.application.dto.StudentDashboardResponse;
import br.edu.lms.module.reporting.application.dto.SubjectAverageGradeResponse;
import br.edu.lms.module.reporting.application.dto.UpcomingTaskResponse;
import br.edu.lms.module.reporting.domain.port.in.GetStudentDashboardUseCase;
import br.edu.lms.module.reporting.domain.port.out.StudentDashboardQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetStudentDashboardService implements GetStudentDashboardUseCase {

    private final StudentDashboardQueryPort studentDashboardQueryPort;

    @Override
    public StudentDashboardResponse execute(String studentId, String organizationId) {
        var upcomingPendingTasks = studentDashboardQueryPort
                .getUpcomingPendingTasks(studentId, organizationId)
                .stream()
                .map(task -> UpcomingTaskResponse.builder()
                        .taskId(task.getTaskId())
                        .title(task.getTitle())
                        .subjectName(task.getSubjectName())
                        .deadline(task.getDeadline())
                        .build())
                .toList();

        var recentGrades = studentDashboardQueryPort
                .getRecentGrades(studentId, organizationId)
                .stream()
                .map(grade -> RecentGradeResponse.builder()
                        .taskId(grade.getTaskId())
                        .title(grade.getTitle())
                        .subjectName(grade.getSubjectName())
                        .grade(grade.getGrade())
                        .feedback(grade.getFeedback())
                        .build())
                .toList();

        var averageGradePerSubject = studentDashboardQueryPort
                .getAverageGradePerSubject(studentId, organizationId)
                .stream()
                .map(subject -> SubjectAverageGradeResponse.builder()
                        .subjectId(subject.getSubjectId())
                        .subjectName(subject.getSubjectName())
                        .averageGrade(subject.getAverageGrade())
                        .build())
                .toList();

        return StudentDashboardResponse.builder()
                .upcomingPendingTasks(upcomingPendingTasks)
                .pendingTasksCount(studentDashboardQueryPort.countPendingTasks(studentId, organizationId))
                .submittedTasksCount(studentDashboardQueryPort.countSubmittedTasks(studentId, organizationId))
                .recentGrades(recentGrades)
                .averageGradePerSubject(averageGradePerSubject)
                .build();
    }
}
