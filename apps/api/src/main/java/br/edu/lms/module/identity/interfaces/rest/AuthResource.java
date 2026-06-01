package br.edu.lms.module.identity.interfaces.rest;

import br.edu.lms.module.identity.application.dto.RegisterUserCommand;
import br.edu.lms.module.identity.domain.port.in.RegisterUserUseCase;
import br.edu.lms.module.identity.interfaces.rest.dto.RegisterRequest;
import br.edu.lms.module.identity.interfaces.rest.dto.RegisterResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public class AuthResource {

    private final RegisterUserUseCase registerUserUseCase;

    @POST
    @Path("/register")
    @Operation(summary = "Cadastro de usuário", description = "Cria uma nova conta. A conta fica ativa apenas após confirmação de e-mail.")
    @APIResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @APIResponse(responseCode = "409", description = "E-mail já em uso")
    @APIResponse(responseCode = "422", description = "Dados inválidos")
    public Response register(@Valid RegisterRequest request) {
        var result = registerUserUseCase.execute(
                RegisterUserCommand.builder()
                        .fullName(request.fullName())
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        return Response.status(Response.Status.CREATED)
                .entity(new RegisterResponse(result.userId(), result.email(), result.status().name()))
                .build();
    }
}
