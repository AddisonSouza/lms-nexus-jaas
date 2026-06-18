package br.edu.lms.module.reporting.domain.port.in;

public interface ExportGestorDashboardPdfUseCase {
    byte[] execute(String organizationId);
}
