package br.edu.lms.module.reporting.domain.port.in;

import br.edu.lms.module.reporting.domain.model.DashboardPeriod;

public interface ExportAdminDashboardPdfUseCase {
    byte[] execute(String organizationId, DashboardPeriod period);
}
