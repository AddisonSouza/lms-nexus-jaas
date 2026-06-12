package br.edu.lms.module.organization.interfaces.rest;

import br.edu.lms.module.organization.application.dto.AcceptInviteCommand;
import br.edu.lms.module.organization.application.dto.InviteMemberCommand;
import br.edu.lms.module.organization.domain.port.in.AcceptInviteUseCase;
import br.edu.lms.module.organization.domain.port.in.GetInvitationInfoUseCase;
import br.edu.lms.module.organization.domain.port.in.InviteMemberUseCase;
import br.edu.lms.module.organization.interfaces.rest.dto.InviteMemberRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Convites de membros para organizações")
public class InvitationResource {

    private final InviteMemberUseCase inviteMemberUseCase;
    private final AcceptInviteUseCase acceptInviteUseCase;
    private final GetInvitationInfoUseCase getInvitationInfoUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/invitations/{token}")
    @Operation(summary = "Obter informações do convite")
    @APIResponse(responseCode = "200", description = "Informações do convite")
    @APIResponse(responseCode = "404", description = "Convite não encontrado")
    public Response info(@PathParam("token") String token) {
        var info = getInvitationInfoUseCase.execute(token);
        return Response.ok(info).build();
    }

    @POST
    @Path("/organizations/{id}/invitations")
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

    @POST
    @Path("/invitations/{token}/accept")
    @Operation(summary = "Aceitar convite")
    @APIResponse(responseCode = "204", description = "Convite aceito")
    @APIResponse(responseCode = "401", description = "Não autenticado")
    @APIResponse(responseCode = "404", description = "Convite não encontrado")
    @APIResponse(responseCode = "409", description = "Convite já utilizado ou usuário já membro")
    @APIResponse(responseCode = "410", description = "Convite expirado")
    public Response accept(@PathParam("token") String token) {
        var userId = jwt.getSubject();
        acceptInviteUseCase.execute(AcceptInviteCommand.builder()
                .token(token)
                .userId(userId)
                .build());
        return Response.noContent().build();
    }
}
