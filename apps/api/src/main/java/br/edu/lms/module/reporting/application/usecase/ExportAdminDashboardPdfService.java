package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.in.ExportAdminDashboardPdfUseCase;
import br.edu.lms.module.reporting.domain.port.in.GetAdminDashboardUseCase;
import br.edu.lms.module.reporting.infrastructure.pdf.DashboardPdfRenderer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ExportAdminDashboardPdfService implements ExportAdminDashboardPdfUseCase {

    private final GetAdminDashboardUseCase getAdminDashboardUseCase;
    private final DashboardPdfRenderer dashboardPdfRenderer;

    @Override
    public byte[] execute(String organizationId, DashboardPeriod period) {
        var dashboard = getAdminDashboardUseCase.execute(organizationId, period);
        return dashboardPdfRenderer.render(dashboard);
    }
}
