package br.edu.lms.module.organization.interfaces.rest;

import br.edu.lms.module.organization.application.dto.CreateOrganizationCommand;
import br.edu.lms.module.organization.application.dto.InviteMemberCommand;
import br.edu.lms.module.organization.application.dto.OrganizationResponse;
import br.edu.lms.module.organization.domain.port.in.ChangeMemberRoleUseCase;
import br.edu.lms.module.organization.domain.port.in.CreateOrganizationUseCase;
import br.edu.lms.module.organization.domain.port.in.InviteMemberUseCase;
import br.edu.lms.module.organization.domain.port.in.ListOrganizationMembersUseCase;
import br.edu.lms.module.organization.domain.port.in.ListUserOrganizationsUseCase;
import br.edu.lms.module.organization.domain.port.in.RemoveMemberUseCase;
import br.edu.lms.module.organization.interfaces.rest.dto.CreateOrganizationRequest;
import br.edu.lms.module.organization.interfaces.rest.dto.ChangeMemberRoleRequest;
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
    private final ListUserOrganizationsUseCase listUserOrganizationsUseCase;
    private final ListOrganizationMembersUseCase listOrganizationMembersUseCase;
    private final ChangeMemberRoleUseCase changeMemberRoleUseCase;
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

    @GET
    @Authenticated
    @Operation(summary = "Listar as organizações do usuário autenticado")
    @APIResponse(responseCode = "200", description = "Organizações do usuário, com o papel em cada uma")
    public Response listMine() {
        return Response.ok(listUserOrganizationsUseCase.execute(jwt.getSubject())).build();
    }

    @POST
    @Path("/{id}/invitations")
    @RolesAllowed("ADMIN_ORG")
    @Operation(summary = "Convidar membro por e-mail")
    @APIResponse(responseCode = "201", description = "Convite enviado")
    @APIResponse(responseCode = "409", description = "Usuário já é membro")
    @APIResponse(responseCode = "403", description = "Sem permissão")
    public Response invite(@PathParam("id") String organizationId, @Valid InviteMemberRequest request) {
        if (!isAdminOf(organizationId)) {
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

    @GET
    @Path("/{id}/members")
    @RolesAllowed("ADMIN_ORG")
    @Operation(summary = "Listar membros da organização")
    @APIResponse(responseCode = "200", description = "Membros ativos, ordenados por nome")
    @APIResponse(responseCode = "403", description = "Sem permissão")
    public Response listMembers(@PathParam("id") String organizationId) {
        if (!isAdminOf(organizationId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok(listOrganizationMembersUseCase.execute(organizationId)).build();
    }

    @PATCH
    @Path("/{id}/members/{userId}")
    @RolesAllowed("ADMIN_ORG")
    @Operation(summary = "Alterar o papel de um membro")
    @APIResponse(responseCode = "204", description = "Papel alterado")
    @APIResponse(responseCode = "403", description = "Sem permissão ou tentativa de alterar o papel do owner")
    @APIResponse(responseCode = "404", description = "Membro não encontrado")
    @APIResponse(responseCode = "422", description = "Papel não atribuível a um membro")
    public Response changeMemberRole(@PathParam("id") String organizationId,
                                     @PathParam("userId") String userId,
                                     @Valid ChangeMemberRoleRequest request) {
        if (!isAdminOf(organizationId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        changeMemberRoleUseCase.execute(organizationId, userId, request.role());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}/members/{userId}")
    @Operation(summary = "Remover membro da organização")
    @APIResponse(responseCode = "204", description = "Membro removido")
    @APIResponse(responseCode = "403", description = "Sem permissão ou tentativa de remover o owner")
    @APIResponse(responseCode = "404", description = "Membro não encontrado")
    public Response removeMember(@PathParam("id") String organizationId,
                                 @PathParam("userId") String userId) {
        if (!isAdminOf(organizationId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        removeMemberUseCase.execute(organizationId, userId);
        return Response.noContent().build();
    }

    /** O ADMIN_ORG só age sobre a organização do próprio token (org do JWT, nunca do path). */
    private boolean isAdminOf(String organizationId) {
        var orgClaim = (String) jwt.getClaim("org");
        var groups = jwt.getGroups();
        return orgClaim != null && orgClaim.equals(organizationId)
                && groups != null && groups.contains("ADMIN_ORG");
    }
}
