package br.edu.lms.module.reporting.infrastructure.pdf;

import br.edu.lms.module.reporting.application.dto.AdminDashboardResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DashboardPdfRendererIT {

    @Inject DashboardPdfRenderer renderer;

    private AdminDashboardResponse dashboard(Map<String, Long> classroomsByStatus, Map<String, Long> membersByRole) {
        return AdminDashboardResponse.builder()
                .from(LocalDate.of(2026, 1, 1))
                .to(LocalDate.of(2026, 1, 31))
                .classroomsByStatus(classroomsByStatus)
                .membersByRole(membersByRole)
                .averageDeliveryRate(BigDecimal.ZERO)
                .activity(List.of())
                .build();
    }

    @Test
    void renderHtml_showsReadableRoleAndClassroomStatusLabels() {
        String html = renderer.renderHtml(dashboard(
                Map.of("ACTIVE", 3L, "ARCHIVED", 1L),
                Map.of("ADMIN_ORG", 1L, "GESTOR", 1L, "PROFESSOR", 2L, "ALUNO", 10L)));

        assertThat(html)
                .contains("<td>Ativa</td>", "<td>Arquivada</td>")
                .contains("<td>Administrador</td>", "<td>Gestor</td>", "<td>Professor</td>", "<td>Aluno</td>")
                .doesNotContain("ADMIN_ORG", "ACTIVE", "ARCHIVED");
    }

    @Test
    void renderHtml_unknownKey_fallsBackToRawValue() {
        String html = renderer.renderHtml(dashboard(Map.of("SUSPENDED", 1L), Map.of("OUTRO", 1L)));

        assertThat(html).contains("<td>SUSPENDED</td>", "<td>OUTRO</td>");
    }
}
