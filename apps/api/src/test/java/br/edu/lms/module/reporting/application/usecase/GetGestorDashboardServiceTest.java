package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.domain.model.AtRiskStudent;
import br.edu.lms.module.reporting.domain.model.ClassroomHealth;
import br.edu.lms.module.reporting.domain.port.out.GestorDashboardQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGestorDashboardServiceTest {

    @Mock GestorDashboardQueryPort gestorDashboardQueryPort;
    @InjectMocks GetGestorDashboardService service;

    private static final String ORG_ID = "org-1";

    @Test
    void execute_aggregatesHealthAndAtRiskStudentsPerClassroom() {
        var health = new ClassroomHealth("c-1", "Turma A", "ACTIVE", BigDecimal.valueOf(0.8), BigDecimal.valueOf(7.5));
        when(gestorDashboardQueryPort.getClassroomsHealth(ORG_ID)).thenReturn(List.of(health));
        when(gestorDashboardQueryPort.listAtRiskStudents("c-1", 5))
                .thenReturn(List.of(new AtRiskStudent("s-1", "Aluno 1", 3)));

        var result = service.execute(ORG_ID);

        assertThat(result.getClassrooms()).hasSize(1);
        var classroom = result.getClassrooms().get(0);
        assertThat(classroom.getClassroomId()).isEqualTo("c-1");
        assertThat(classroom.getClassroomName()).isEqualTo("Turma A");
        assertThat(classroom.getStatus()).isEqualTo("ACTIVE");
        assertThat(classroom.getDeliveryRate()).isEqualTo(BigDecimal.valueOf(0.8));
        assertThat(classroom.getAverageGrade()).isEqualTo(BigDecimal.valueOf(7.5));
        assertThat(classroom.getAtRiskStudents()).extracting("studentName").containsExactly("Aluno 1");
        assertThat(classroom.getAtRiskStudents().get(0).getPendingCount()).isEqualTo(3);
    }

    @Test
    void execute_classroomWithoutEvaluatedSubmissions_returnsNullAverageGrade() {
        var health = new ClassroomHealth("c-1", "Turma A", "ACTIVE", BigDecimal.ZERO, null);
        when(gestorDashboardQueryPort.getClassroomsHealth(ORG_ID)).thenReturn(List.of(health));
        when(gestorDashboardQueryPort.listAtRiskStudents("c-1", 5)).thenReturn(List.of());

        var result = service.execute(ORG_ID);

        assertThat(result.getClassrooms().get(0).getAverageGrade()).isNull();
        assertThat(result.getClassrooms().get(0).getAtRiskStudents()).isEmpty();
    }

    @Test
    void execute_organizationWithoutClassrooms_returnsEmptyList() {
        when(gestorDashboardQueryPort.getClassroomsHealth(ORG_ID)).thenReturn(List.of());

        var result = service.execute(ORG_ID);

        assertThat(result.getClassrooms()).isEmpty();
    }
}
