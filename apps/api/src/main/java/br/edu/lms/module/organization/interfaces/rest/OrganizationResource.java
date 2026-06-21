package br.edu.lms.module.organization.interfaces.rest;

import br.edu.lms.module.organization.application.dto.CreateOrganizationCommand;
import br.edu.lms.module.organization.application.dto.InviteMemberCommand;
import br.edu.lms.module.organization.application.dto.OrganizationResponse;
import br.edu.lms.module.organization.domain.port.in.CreateOrganizationUseCase;
import br.edu.lms.module.organization.domain.port.in.InviteMemberUseCase;
import br.edu.lms.module.organization.domain.port.in.RemoveMemberUseCase;
import br.edu.lms.module.organization.interfaces.rest.dto.CreateOrganizationRequest;
import br.edu.lms.module.organization.interfaces.rest.dto.InviteMemberRequest;
import io.quarkus.security.Authenticated;
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
    private final RemoveMemberUseCase removeMemberUseCase;
    private final InviteMemberUseCase inviteMemberUseCase;
    private final JsonWebToken jwt;

    @POST
    @Authenticated
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

    @POST
    @Path("/{id}/invitations")
    @RolesAllowed("ADMIN_ORG")
    @Operation(summary = "Convidar membro por e-mail")
    @APIResponse(responseCode = "201", description = "Convite enviado")
    @APIResponse(responseCode = "409", description = "Usuário já é membro")
    @APIResponse(responseCode = "403", description = "Sem permissão")
    public Response invite(@PathParam("id") String organizationId, @Valid InviteMemberRequest request) {
        var orgClaim = (String) jwt.getClaim("org");
        var groups = jwt.getGroups();
        if (orgClaim == null || !orgClaim.equals(organizationId) || groups == null || !groups.contains("ADMIN_ORG")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        inviteMemberUseCase.execute(InviteMemberCommand.builder()
                .organizationId(organizationId)
                .email(request.email())
                .role(request.role())
                .invitedBy(jwt.getSubject())
                .build());
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{id}/members/{userId}")
    @Operation(summary = "Remover membro da organização")
    @APIResponse(responseCode = "204", description = "Membro removido")
    @APIResponse(responseCode = "403", description = "Sem permissão ou tentativa de remover o owner")
    @APIResponse(responseCode = "404", description = "Membro não encontrado")
    public Response removeMember(@PathParam("id") String organizationId,
                                 @PathParam("userId") String userId) {
        var orgClaim = (String) jwt.getClaim("org");
        var groups = jwt.getGroups();
        if (orgClaim == null || !orgClaim.equals(organizationId) || groups == null || !groups.contains("ADMIN_ORG")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        removeMemberUseCase.execute(organizationId, userId);
        return Response.noContent().build();
    }
}
