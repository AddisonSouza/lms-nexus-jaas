package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.domain.port.in.ExportGestorDashboardPdfUseCase;
import br.edu.lms.module.reporting.domain.port.in.GetGestorDashboardUseCase;
import br.edu.lms.module.reporting.infrastructure.pdf.DashboardPdfRenderer;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ExportGestorDashboardPdfService implements ExportGestorDashboardPdfUseCase {

    private final GetGestorDashboardUseCase getGestorDashboardUseCase;
    private final DashboardPdfRenderer dashboardPdfRenderer;

    @Override
    public byte[] execute(String organizationId) {
        var dashboard = getGestorDashboardUseCase.execute(organizationId);
        return dashboardPdfRenderer.renderGestorDashboard(dashboard);
    }
}
