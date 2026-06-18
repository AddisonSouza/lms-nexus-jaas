package br.edu.lms.module.reporting.interfaces.rest;

import br.edu.lms.module.reporting.domain.port.in.GetStudentDashboardUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/students/me")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Dashboard do aluno")
public class StudentDashboardResource {

    private final GetStudentDashboardUseCase getStudentDashboardUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/dashboard")
    @RolesAllowed("ALUNO")
    @Operation(summary = "Dashboard do aluno com próximas tarefas, entregas e notas")
    @APIResponse(responseCode = "200", description = "Indicadores do aluno autenticado")
    public Response getDashboard() {
        var organizationId = (String) jwt.getClaim("org");
        var dashboard = getStudentDashboardUseCase.execute(jwt.getSubject(), organizationId);
        return Response.ok(dashboard).build();
    }
}
