package br.edu.lms.module.reporting.interfaces.rest;

import br.edu.lms.module.reporting.domain.port.in.GetProfessorDashboardUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/subjects/{id}")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Dashboard do professor por disciplina")
public class ProfessorDashboardResource {

    private final GetProfessorDashboardUseCase getProfessorDashboardUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/dashboard")
    @RolesAllowed("PROFESSOR")
    @Operation(summary = "Dashboard do professor com indicadores da disciplina")
    @APIResponse(responseCode = "200", description = "Indicadores da disciplina")
    @APIResponse(responseCode = "403", description = "Professor não vinculado a esta disciplina")
    public Response getDashboard(@PathParam("id") String subjectId) {
        var dashboard = getProfessorDashboardUseCase.execute(subjectId, jwt.getSubject());
        return Response.ok(dashboard).build();
    }
}
