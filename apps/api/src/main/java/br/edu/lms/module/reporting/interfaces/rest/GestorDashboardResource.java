package br.edu.lms.module.reporting.interfaces.rest;

import br.edu.lms.module.reporting.domain.port.in.ExportGestorDashboardPdfUseCase;
import br.edu.lms.module.reporting.domain.port.in.GetGestorDashboardUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Dashboard e relatórios da organização")
public class GestorDashboardResource {

    private final GetGestorDashboardUseCase getGestorDashboardUseCase;
    private final ExportGestorDashboardPdfUseCase exportGestorDashboardPdfUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/{id}/gestor-dashboard")
    @RolesAllowed("GESTOR")
    @Operation(summary = "Dashboard do gestor com saúde das turmas da organização")
    @APIResponse(responseCode = "200", description = "Saúde das turmas da organização")
    @APIResponse(responseCode = "403", description = "Sem permissão para acessar esta organização")
    public Response getDashboard(@PathParam("id") String organizationId) {
        var forbidden = checkOrganizationAccess(organizationId);
        if (forbidden != null) {
            return forbidden;
        }
        var dashboard = getGestorDashboardUseCase.execute(organizationId);
        return Response.ok(dashboard).build();
    }

    @GET
    @Path("/{id}/gestor-dashboard/pdf")
    @RolesAllowed("GESTOR")
    @Produces("application/pdf")
    @Operation(summary = "Exportação do dashboard do gestor em PDF")
    @APIResponse(responseCode = "200", description = "PDF do dashboard do gestor")
    @APIResponse(responseCode = "403", description = "Sem permissão para acessar esta organização")
    public Response exportDashboardPdf(@PathParam("id") String organizationId) {
        var forbidden = checkOrganizationAccess(organizationId);
        if (forbidden != null) {
            return forbidden;
        }
        byte[] pdf = exportGestorDashboardPdfUseCase.execute(organizationId);
        return Response.ok(pdf)
                .header("Content-Disposition", "attachment; filename=\"gestor-dashboard.pdf\"")
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
