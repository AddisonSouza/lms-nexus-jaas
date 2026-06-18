package br.edu.lms.module.reporting.infrastructure.pdf;

import br.edu.lms.module.reporting.application.dto.AdminDashboardResponse;
import br.edu.lms.module.reporting.application.dto.GestorDashboardResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;

@ApplicationScoped
public class DashboardPdfRenderer {

    @Inject
    @Location("reporting/dashboard.html")
    Template dashboardTemplate;

    @Inject
    @Location("reporting/gestor-dashboard.html")
    Template gestorDashboardTemplate;

    public byte[] render(AdminDashboardResponse dashboard) {
        return toPdf(dashboardTemplate.data("dashboard", dashboard).render());
    }

    public byte[] renderGestorDashboard(GestorDashboardResponse dashboard) {
        return toPdf(gestorDashboardTemplate.data("dashboard", dashboard).render());
    }

    private byte[] toPdf(String html) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(output);
            builder.run();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar PDF do dashboard", e);
        }
    }
}
