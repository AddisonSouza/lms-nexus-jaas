package br.edu.lms.module.reporting.domain.port.in;

import br.edu.lms.module.reporting.application.dto.AdminDashboardResponse;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;

public interface GetAdminDashboardUseCase {
    AdminDashboardResponse execute(String organizationId, DashboardPeriod period);
}
