package br.edu.lms.module.reporting.domain.port.out;

import br.edu.lms.module.reporting.domain.model.RecentGrade;
import br.edu.lms.module.reporting.domain.model.SubjectAverageGrade;
import br.edu.lms.module.reporting.domain.model.UpcomingTask;

import java.util.List;

public interface StudentDashboardQueryPort {
    List<UpcomingTask> getUpcomingPendingTasks(String studentId, String organizationId);

    long countPendingTasks(String studentId, String organizationId);

    long countSubmittedTasks(String studentId, String organizationId);

    List<RecentGrade> getRecentGrades(String studentId, String organizationId);

    List<SubjectAverageGrade> getAverageGradePerSubject(String studentId, String organizationId);
}
