package br.edu.lms.module.reporting.interfaces.rest;

import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.in.ExportAdminDashboardPdfUseCase;
import br.edu.lms.module.reporting.domain.port.in.GetAdminDashboardUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;

@Path("/organizations/{id}")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Dashboard e relatórios da organização")
public class AdminDashboardResource {

    private final GetAdminDashboardUseCase getAdminDashboardUseCase;
    private final ExportAdminDashboardPdfUseCase exportAdminDashboardPdfUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/dashboard")
    @RolesAllowed("ADMIN_ORG")
    @Operation(summary = "Dashboard administrativo da organização")
    @APIResponse(responseCode = "200", description = "Métricas da organização no período")
    @APIResponse(responseCode = "400", description = "Período inválido")
    @APIResponse(responseCode = "403", description = "Sem permissão para acessar esta organização")
    public Response getDashboard(@PathParam("id") String organizationId,
                                  @QueryParam("from") LocalDate from,
                                  @QueryParam("to") LocalDate to) {
        var forbidden = checkOrganizationAccess(organizationId);
        if (forbidden != null) {
            return forbidden;
        }
        var dashboard = getAdminDashboardUseCase.execute(organizationId, new DashboardPeriod(from, to));
        return Response.ok(dashboard).build();
    }

    @GET
    @Path("/reports/pdf")
    @RolesAllowed("ADMIN_ORG")
    @Produces("application/pdf")
    @Operation(summary = "Exportação do dashboard administrativo em PDF")
    @APIResponse(responseCode = "200", description = "PDF do dashboard")
    @APIResponse(responseCode = "400", description = "Período inválido")
    @APIResponse(responseCode = "403", description = "Sem permissão para acessar esta organização")
    public Response exportDashboardPdf(@PathParam("id") String organizationId,
                                        @QueryParam("from") LocalDate from,
                                        @QueryParam("to") LocalDate to) {
        var forbidden = checkOrganizationAccess(organizationId);
        if (forbidden != null) {
            return forbidden;
        }
        byte[] pdf = exportAdminDashboardPdfUseCase.execute(organizationId, new DashboardPeriod(from, to));
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=\"dashboard.pdf\"")
                .build();
    }

    private Response checkOrganizationAccess(String organizationId) {
        var orgClaim = (String) jwt.getClaim("org");
        if (orgClaim == null || !orgClaim.equals(organizationId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return null;
    }
}
