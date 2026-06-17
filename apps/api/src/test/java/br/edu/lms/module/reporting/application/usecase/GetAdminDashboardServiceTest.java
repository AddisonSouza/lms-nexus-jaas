package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.ActivityType;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.out.ClassroomMetricsQueryPort;
import br.edu.lms.module.reporting.domain.port.out.MemberMetricsQueryPort;
import br.edu.lms.module.reporting.domain.port.out.TaskMetricsQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdminDashboardServiceTest {

    @Mock ClassroomMetricsQueryPort classroomMetricsQueryPort;
    @Mock MemberMetricsQueryPort memberMetricsQueryPort;
    @Mock TaskMetricsQueryPort taskMetricsQueryPort;
    @InjectMocks GetAdminDashboardService service;

    private static final String ORG_ID = "org-1";
    private final DashboardPeriod period = new DashboardPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

    @Test
    void execute_aggregatesMetricsFromAllPorts() {
        when(classroomMetricsQueryPort.countByStatus(ORG_ID)).thenReturn(Map.of("ACTIVE", 3L, "ARCHIVED", 1L));
        when(memberMetricsQueryPort.countByRole(ORG_ID)).thenReturn(Map.of("ALUNO", 10L, "PROFESSOR", 2L));
        when(taskMetricsQueryPort.countCreated(ORG_ID, period)).thenReturn(5L);
        when(taskMetricsQueryPort.countEvaluated(ORG_ID, period)).thenReturn(3L);
        when(taskMetricsQueryPort.averageDeliveryRate(ORG_ID, period)).thenReturn(BigDecimal.valueOf(0.75));
        when(classroomMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of());
        when(memberMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of());
        when(taskMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of());

        var result = service.execute(ORG_ID, period);

        assertThat(result.getClassroomsByStatus()).containsEntry("ACTIVE", 3L).containsEntry("ARCHIVED", 1L);
        assertThat(result.getMembersByRole()).containsEntry("ALUNO", 10L).containsEntry("PROFESSOR", 2L);
        assertThat(result.getTasksCreated()).isEqualTo(5L);
        assertThat(result.getTasksEvaluated()).isEqualTo(3L);
        assertThat(result.getAverageDeliveryRate()).isEqualTo(BigDecimal.valueOf(0.75));
        assertThat(result.getFrom()).isEqualTo(period.getFrom());
        assertThat(result.getTo()).isEqualTo(period.getTo());
    }

    @Test
    void execute_mergesAndSortsActivityFromAllPortsByDateDescending() {
        when(classroomMetricsQueryPort.countByStatus(ORG_ID)).thenReturn(Map.of());
        when(memberMetricsQueryPort.countByRole(ORG_ID)).thenReturn(Map.of());
        when(taskMetricsQueryPort.countCreated(ORG_ID, period)).thenReturn(0L);
        when(taskMetricsQueryPort.countEvaluated(ORG_ID, period)).thenReturn(0L);
        when(taskMetricsQueryPort.averageDeliveryRate(ORG_ID, period)).thenReturn(BigDecimal.ZERO);

        var oldest = new ActivityItem(ActivityType.CLASSROOM_CREATED, "c-1", "Turma criada", LocalDateTime.of(2026, 1, 5, 10, 0));
        var newest = new ActivityItem(ActivityType.MEMBER_JOINED, "m-1", "Membro ingressou", LocalDateTime.of(2026, 1, 20, 10, 0));
        var middle = new ActivityItem(ActivityType.TASK_CREATED, "t-1", "Tarefa criada", LocalDateTime.of(2026, 1, 10, 10, 0));

        when(classroomMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of(oldest));
        when(memberMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of(newest));
        when(taskMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of(middle));

        var result = service.execute(ORG_ID, period);

        assertThat(result.getActivity()).extracting("referenceId").containsExactly("m-1", "t-1", "c-1");
    }

    @Test
    void execute_emptyPeriod_returnsZeroedMetricsWithoutError() {
        when(classroomMetricsQueryPort.countByStatus(ORG_ID)).thenReturn(Map.of());
        when(memberMetricsQueryPort.countByRole(ORG_ID)).thenReturn(Map.of());
        when(taskMetricsQueryPort.countCreated(ORG_ID, period)).thenReturn(0L);
        when(taskMetricsQueryPort.countEvaluated(ORG_ID, period)).thenReturn(0L);
        when(taskMetricsQueryPort.averageDeliveryRate(ORG_ID, period)).thenReturn(BigDecimal.ZERO);
        when(classroomMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of());
        when(memberMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of());
        when(taskMetricsQueryPort.listActivity(ORG_ID, period)).thenReturn(List.of());

        var result = service.execute(ORG_ID, period);

        assertThat(result.getClassroomsByStatus()).isEmpty();
        assertThat(result.getTasksCreated()).isZero();
        assertThat(result.getAverageDeliveryRate()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getActivity()).isEmpty();
    }
}
