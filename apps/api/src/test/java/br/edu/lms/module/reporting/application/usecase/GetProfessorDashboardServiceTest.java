package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.domain.exception.UnauthorizedDashboardAccessException;
import br.edu.lms.module.reporting.domain.model.StudentAverageGrade;
import br.edu.lms.module.reporting.domain.model.StudentSummary;
import br.edu.lms.module.reporting.domain.port.out.ProfessorDashboardQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class GetProfessorDashboardServiceTest {

    @Mock ProfessorDashboardQueryPort professorDashboardQueryPort;
    @InjectMocks GetProfessorDashboardService service;

    private static final String SUBJECT_ID = "subject-1";
    private static final String PROFESSOR_ID = "professor-1";

    @Test
    void execute_professorAssigned_returnsFullDashboard() {
        when(professorDashboardQueryPort.isProfessorAssignedToSubject(SUBJECT_ID, PROFESSOR_ID)).thenReturn(true);
        when(professorDashboardQueryPort.countPendingEvaluations(SUBJECT_ID)).thenReturn(3L);
        when(professorDashboardQueryPort.getLastTaskGradeDistribution(SUBJECT_ID))
                .thenReturn(List.of(BigDecimal.valueOf(8.5), BigDecimal.valueOf(7.0)));
        when(professorDashboardQueryPort.getLastTaskStudentsWithoutSubmission(SUBJECT_ID))
                .thenReturn(List.of(new StudentSummary("s-1", "Aluno 1")));
        when(professorDashboardQueryPort.getAverageGradePerStudent(SUBJECT_ID))
                .thenReturn(List.of(new StudentAverageGrade("s-2", "Aluno 2", BigDecimal.valueOf(9.0))));

        var result = service.execute(SUBJECT_ID, PROFESSOR_ID);

        assertThat(result.getPendingEvaluationsCount()).isEqualTo(3L);
        assertThat(result.getLastTaskGradeDistribution()).containsExactly(BigDecimal.valueOf(8.5), BigDecimal.valueOf(7.0));
        assertThat(result.getStudentsWithoutSubmission()).extracting("studentName").containsExactly("Aluno 1");
        assertThat(result.getAverageGradePerStudent()).extracting("studentName").containsExactly("Aluno 2");
    }

    @Test
    void execute_professorNotAssigned_throwsUnauthorized() {
        when(professorDashboardQueryPort.isProfessorAssignedToSubject(SUBJECT_ID, PROFESSOR_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.execute(SUBJECT_ID, PROFESSOR_ID))
                .isInstanceOf(UnauthorizedDashboardAccessException.class);

        verifyNoMoreInteractions(professorDashboardQueryPort);
    }

    @Test
    void execute_subjectWithoutTasks_returnsEmptyIndicators() {
        when(professorDashboardQueryPort.isProfessorAssignedToSubject(SUBJECT_ID, PROFESSOR_ID)).thenReturn(true);
        when(professorDashboardQueryPort.countPendingEvaluations(SUBJECT_ID)).thenReturn(0L);
        when(professorDashboardQueryPort.getLastTaskGradeDistribution(SUBJECT_ID)).thenReturn(List.of());
        when(professorDashboardQueryPort.getLastTaskStudentsWithoutSubmission(SUBJECT_ID)).thenReturn(List.of());
        when(professorDashboardQueryPort.getAverageGradePerStudent(SUBJECT_ID)).thenReturn(List.of());

        var result = service.execute(SUBJECT_ID, PROFESSOR_ID);

        assertThat(result.getPendingEvaluationsCount()).isZero();
        assertThat(result.getLastTaskGradeDistribution()).isEmpty();
        assertThat(result.getStudentsWithoutSubmission()).isEmpty();
        assertThat(result.getAverageGradePerStudent()).isEmpty();
    }
}
