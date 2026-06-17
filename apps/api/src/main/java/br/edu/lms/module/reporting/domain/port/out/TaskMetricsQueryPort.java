package br.edu.lms.module.reporting.domain.port.out;

import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;

import java.math.BigDecimal;
import java.util.List;

public interface TaskMetricsQueryPort {
    long countCreated(String organizationId, DashboardPeriod period);
    long countEvaluated(String organizationId, DashboardPeriod period);
    BigDecimal averageDeliveryRate(String organizationId, DashboardPeriod period);
    List<ActivityItem> listActivity(String organizationId, DashboardPeriod period);
}
