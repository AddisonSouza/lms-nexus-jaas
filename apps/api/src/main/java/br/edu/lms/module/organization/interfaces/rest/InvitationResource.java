package br.edu.lms.module.organization.interfaces.rest;

import br.edu.lms.module.organization.application.dto.AcceptInviteCommand;
import br.edu.lms.module.organization.domain.port.in.AcceptInviteUseCase;
import br.edu.lms.module.organization.domain.port.in.GetInvitationInfoUseCase;
import br.edu.lms.module.organization.domain.port.in.ListPendingInvitationsUseCase;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/invitations")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Convites de membros para organizações")
public class InvitationResource {

    private final AcceptInviteUseCase acceptInviteUseCase;
    private final GetInvitationInfoUseCase getInvitationInfoUseCase;
    private final ListPendingInvitationsUseCase listPendingInvitationsUseCase;
    private final JsonWebToken jwt;

    @GET
    @Path("/pending")
    @Authenticated
    @Operation(summary = "Listar os convites pendentes endereçados ao usuário autenticado")
    @APIResponse(responseCode = "200", description = "Convites pendentes, do mais recente ao mais antigo")
    @APIResponse(responseCode = "401", description = "Não autenticado")
    public Response pending() {
        return Response.ok(listPendingInvitationsUseCase.execute(jwt.getSubject())).build();
    }

    @GET
    @Path("/{token}")
    @Operation(summary = "Obter informações do convite")
    @APIResponse(responseCode = "200", description = "Informações do convite")
    @APIResponse(responseCode = "404", description = "Convite não encontrado")
    public Response info(@PathParam("token") String token) {
        var info = getInvitationInfoUseCase.execute(token);
        return Response.ok(info).build();
    }

    @POST
    @Path("/{token}/accept")
    @Authenticated
    @Operation(summary = "Aceitar convite")
    @APIResponse(responseCode = "204", description = "Convite aceito")
    @APIResponse(responseCode = "401", description = "Não autenticado")
    @APIResponse(responseCode = "403", description = "Convite endereçado a outro e-mail")
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
