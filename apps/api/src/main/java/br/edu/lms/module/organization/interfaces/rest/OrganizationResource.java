package br.edu.lms.module.organization.interfaces.rest;

import br.edu.lms.module.organization.application.dto.CreateOrganizationCommand;
import br.edu.lms.module.organization.application.dto.OrganizationResponse;
import br.edu.lms.module.organization.domain.port.in.CreateOrganizationUseCase;
import br.edu.lms.module.organization.interfaces.rest.dto.CreateOrganizationRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;

@Path("/organizations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Gestão de organizações educacionais")
public class OrganizationResource {

    private final CreateOrganizationUseCase createOrganizationUseCase;
    private final JsonWebToken jwt;

    @POST
    @Operation(summary = "Criar organização")
    @APIResponse(responseCode = "201", description = "Organização criada com sucesso")
    @APIResponse(responseCode = "409", description = "Nome já utilizado por este usuário")
    public Response create(@Valid CreateOrganizationRequest request) {
        var userId = jwt.getSubject();
        var result = createOrganizationUseCase.execute(
                CreateOrganizationCommand.builder()
                        .name(request.name())
                        .description(request.description())
                        .ownerId(userId)
                        .build());
        return Response.created(URI.create("/organizations/" + result.getId()))
                .entity(result)
                .build();
    }
}
