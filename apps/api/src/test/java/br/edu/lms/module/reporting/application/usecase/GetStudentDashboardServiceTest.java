package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.domain.model.RecentGrade;
import br.edu.lms.module.reporting.domain.model.SubjectAverageGrade;
import br.edu.lms.module.reporting.domain.model.UpcomingTask;
import br.edu.lms.module.reporting.domain.port.out.StudentDashboardQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStudentDashboardServiceTest {

    @Mock StudentDashboardQueryPort studentDashboardQueryPort;
    @InjectMocks GetStudentDashboardService service;

    private static final String STUDENT_ID = "student-1";
    private static final String ORG_ID = "org-1";

    @Test
    void execute_studentWithData_returnsFullDashboard() {
        var deadline = LocalDateTime.now().plusDays(3);
        when(studentDashboardQueryPort.getUpcomingPendingTasks(STUDENT_ID, ORG_ID))
                .thenReturn(List.of(new UpcomingTask("task-1", "Tarefa 1", "Disciplina 1", deadline)));
        when(studentDashboardQueryPort.countPendingTasks(STUDENT_ID, ORG_ID)).thenReturn(1L);
        when(studentDashboardQueryPort.countSubmittedTasks(STUDENT_ID, ORG_ID)).thenReturn(2L);
        when(studentDashboardQueryPort.getRecentGrades(STUDENT_ID, ORG_ID))
                .thenReturn(List.of(new RecentGrade("task-0", "Tarefa 0", "Disciplina 1", BigDecimal.valueOf(8.5), "Bom trabalho")));
        when(studentDashboardQueryPort.getAverageGradePerSubject(STUDENT_ID, ORG_ID))
                .thenReturn(List.of(new SubjectAverageGrade("subject-1", "Disciplina 1", BigDecimal.valueOf(8.5))));

        var result = service.execute(STUDENT_ID, ORG_ID);

        assertThat(result.getUpcomingPendingTasks()).extracting("title").containsExactly("Tarefa 1");
        assertThat(result.getPendingTasksCount()).isEqualTo(1L);
        assertThat(result.getSubmittedTasksCount()).isEqualTo(2L);
        assertThat(result.getRecentGrades()).extracting("grade").containsExactly(BigDecimal.valueOf(8.5));
        assertThat(result.getAverageGradePerSubject()).extracting("subjectName").containsExactly("Disciplina 1");
    }

    @Test
    void execute_studentWithoutAnyTask_returnsEmptyIndicators() {
        when(studentDashboardQueryPort.getUpcomingPendingTasks(STUDENT_ID, ORG_ID)).thenReturn(List.of());
        when(studentDashboardQueryPort.countPendingTasks(STUDENT_ID, ORG_ID)).thenReturn(0L);
        when(studentDashboardQueryPort.countSubmittedTasks(STUDENT_ID, ORG_ID)).thenReturn(0L);
        when(studentDashboardQueryPort.getRecentGrades(STUDENT_ID, ORG_ID)).thenReturn(List.of());
        when(studentDashboardQueryPort.getAverageGradePerSubject(STUDENT_ID, ORG_ID)).thenReturn(List.of());

        var result = service.execute(STUDENT_ID, ORG_ID);

        assertThat(result.getUpcomingPendingTasks()).isEmpty();
        assertThat(result.getPendingTasksCount()).isZero();
        assertThat(result.getSubmittedTasksCount()).isZero();
        assertThat(result.getRecentGrades()).isEmpty();
        assertThat(result.getAverageGradePerSubject()).isEmpty();
    }
}
