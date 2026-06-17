package br.edu.lms.module.reporting.domain.port.out;

import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;

import java.util.List;
import java.util.Map;

public interface MemberMetricsQueryPort {
    Map<String, Long> countByRole(String organizationId);
    List<ActivityItem> listActivity(String organizationId, DashboardPeriod period);
}
