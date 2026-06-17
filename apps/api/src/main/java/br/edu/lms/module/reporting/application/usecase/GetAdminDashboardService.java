package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.application.dto.ActivityItemResponse;
import br.edu.lms.module.reporting.application.dto.AdminDashboardResponse;
import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.in.GetAdminDashboardUseCase;
import br.edu.lms.module.reporting.domain.port.out.ClassroomMetricsQueryPort;
import br.edu.lms.module.reporting.domain.port.out.MemberMetricsQueryPort;
import br.edu.lms.module.reporting.domain.port.out.TaskMetricsQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class GetAdminDashboardService implements GetAdminDashboardUseCase {

    private final ClassroomMetricsQueryPort classroomMetricsQueryPort;
    private final MemberMetricsQueryPort memberMetricsQueryPort;
    private final TaskMetricsQueryPort taskMetricsQueryPort;

    @Override
    public AdminDashboardResponse execute(String organizationId, DashboardPeriod period) {
        var classroomsByStatus = classroomMetricsQueryPort.countByStatus(organizationId);
        var membersByRole = memberMetricsQueryPort.countByRole(organizationId);
        var tasksCreated = taskMetricsQueryPort.countCreated(organizationId, period);
        var tasksEvaluated = taskMetricsQueryPort.countEvaluated(organizationId, period);
        var averageDeliveryRate = taskMetricsQueryPort.averageDeliveryRate(organizationId, period);

        List<ActivityItem> activity = new ArrayList<>();
        activity.addAll(classroomMetricsQueryPort.listActivity(organizationId, period));
        activity.addAll(memberMetricsQueryPort.listActivity(organizationId, period));
        activity.addAll(taskMetricsQueryPort.listActivity(organizationId, period));
        activity.sort(Comparator.comparing(ActivityItem::getOccurredAt).reversed());

        return AdminDashboardResponse.builder()
                .from(period.getFrom())
                .to(period.getTo())
                .classroomsByStatus(classroomsByStatus)
                .membersByRole(membersByRole)
                .tasksCreated(tasksCreated)
                .tasksEvaluated(tasksEvaluated)
                .averageDeliveryRate(averageDeliveryRate)
                .activity(activity.stream()
                        .map(item -> ActivityItemResponse.builder()
                                .type(item.getType().name())
                                .referenceId(item.getReferenceId())
                                .description(item.getDescription())
                                .occurredAt(item.getOccurredAt())
                                .build())
                        .toList())
                .build();
    }
}
