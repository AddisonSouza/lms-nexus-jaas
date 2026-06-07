package br.edu.lms.module.identity.interfaces.rest;

import br.edu.lms.module.identity.application.dto.AuthenticateCommand;
import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.application.dto.RegisterUserCommand;
import br.edu.lms.module.identity.application.dto.RequestPasswordResetCommand;
import br.edu.lms.module.identity.application.dto.ResetPasswordCommand;
import br.edu.lms.module.identity.domain.port.in.AuthenticateUseCase;
import br.edu.lms.module.identity.domain.port.in.LogoutUseCase;
import br.edu.lms.module.identity.domain.port.in.RefreshTokenUseCase;
import br.edu.lms.module.identity.domain.port.in.RegisterUserUseCase;
import br.edu.lms.module.identity.domain.port.in.RequestPasswordResetUseCase;
import br.edu.lms.module.identity.domain.port.in.ResetPasswordUseCase;
import br.edu.lms.module.identity.interfaces.rest.dto.ForgotPasswordRequest;
import br.edu.lms.module.identity.interfaces.rest.dto.LoginRequest;
import br.edu.lms.module.identity.interfaces.rest.dto.LoginResponse;
import br.edu.lms.module.identity.interfaces.rest.dto.RegisterRequest;
import br.edu.lms.module.identity.interfaces.rest.dto.RegisterResponse;
import br.edu.lms.module.identity.interfaces.rest.dto.ResetPasswordRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.Duration;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public class AuthResource {

    private static final String REFRESH_COOKIE = "__refresh_token";
    private static final int COOKIE_MAX_AGE_SECONDS = (int) Duration.ofDays(7).toSeconds();

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUseCase authenticateUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @POST
    @Path("/register")
    @Operation(summary = "Cadastro de usuário")
    @APIResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @APIResponse(responseCode = "409", description = "E-mail já em uso")
    public Response register(@Valid RegisterRequest request) {
        var result = registerUserUseCase.execute(
                RegisterUserCommand.builder()
                        .fullName(request.fullName())
                        .email(request.email())
                        .rawPassword(request.password())
                        .build());
        return Response.status(Response.Status.CREATED)
                .entity(new RegisterResponse(result.userId(), result.email(), result.status().name()))
                .build();
    }

    @POST
    @Path("/login")
    @Operation(summary = "Login com e-mail e senha")
    @APIResponse(responseCode = "200", description = "Autenticado com sucesso")
    @APIResponse(responseCode = "401", description = "Credenciais inválidas")
    public Response login(@Valid LoginRequest request) {
        var result = authenticateUseCase.execute(
                new AuthenticateCommand(request.email(), request.password()));

        var cookie = new NewCookie.Builder(REFRESH_COOKIE)
                .value(result.refreshToken())
                .path("/auth")
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .build();

        return Response.ok(new LoginResponse(result.accessToken()))
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Logout — invalida o refresh token")
    @APIResponse(responseCode = "204", description = "Logout realizado")
    @APIResponse(responseCode = "401", description = "Token ausente ou inválido")
    public Response logout(@CookieParam(REFRESH_COOKIE) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        logoutUseCase.execute(refreshToken);
        var clearCookie = new NewCookie.Builder(REFRESH_COOKIE)
                .value("")
                .path("/auth")
                .httpOnly(true)
                .maxAge(0)
                .build();
        return Response.noContent().cookie(clearCookie).build();
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Renova o par de tokens")
    @APIResponse(responseCode = "200", description = "Tokens renovados")
    @APIResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    public Response refresh(@CookieParam(REFRESH_COOKIE) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        var result = refreshTokenUseCase.execute(new RefreshCommand(refreshToken));

        var cookie = new NewCookie.Builder(REFRESH_COOKIE)
                .value(result.refreshToken())
                .path("/auth")
                .httpOnly(true)
                .secure(true)
                .sameSite(NewCookie.SameSite.STRICT)
                .maxAge(COOKIE_MAX_AGE_SECONDS)
                .build();

        return Response.ok(new LoginResponse(result.accessToken())).cookie(cookie).build();
    }

    @POST
    @Path("/forgot-password")
    @Operation(summary = "Solicitar redefinição de senha")
    @APIResponse(responseCode = "204", description = "Solicitação processada")
    public Response forgotPassword(@Valid ForgotPasswordRequest request) {
        requestPasswordResetUseCase.execute(
                RequestPasswordResetCommand.builder()
                        .email(request.email())
                        .build());
        return Response.noContent().build();
    }

    @POST
    @Path("/reset-password")
    @Operation(summary = "Confirmar redefinição de senha")
    @APIResponse(responseCode = "204", description = "Senha redefinida com sucesso")
    @APIResponse(responseCode = "400", description = "Token inválido, expirado ou já utilizado")
    public Response resetPassword(@Valid ResetPasswordRequest request) {
        resetPasswordUseCase.execute(
                ResetPasswordCommand.builder()
                        .token(request.token())
                        .newPassword(request.newPassword())
                        .build());
        return Response.noContent().build();
    }
}
