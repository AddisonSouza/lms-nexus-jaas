package br.edu.lms.module.reporting.infrastructure.pdf;

import br.edu.lms.module.reporting.application.dto.AdminDashboardResponse;
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

    public byte[] render(AdminDashboardResponse dashboard) {
        String html = dashboardTemplate.data("dashboard", dashboard).render();

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
