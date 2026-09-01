package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.StaleSessionRepository;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;

/**
 * Recusa o access token emitido antes de o vínculo do usuário mudar. Responde
 * 401 — e não 403 — de propósito: é o 401 que o front trata renovando o token em
 * silêncio e refazendo a requisição, e a renovação relê o papel do banco. Quem
 * teve o papel alterado nem percebe; só passa a agir com o papel novo.
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 10)
@ApplicationScoped
@RequiredArgsConstructor
public class StaleSessionFilter implements ContainerRequestFilter {

    private final StaleSessionRepository staleSessionRepository;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Sem token não há sessão a invalidar: o filtro não opina sobre endpoint
        // público nem sobre o próprio refresh, que não manda o access token.
        if (!(requestContext.getSecurityContext().getUserPrincipal() instanceof JsonWebToken jwt)) {
            return;
        }

        var staleSince = staleSessionRepository.staleSince(jwt.getSubject());

        // O iat tem precisão de segundo: um token emitido no mesmo segundo da
        // marca passa. A janela residual fica abaixo de um segundo.
        if (staleSince.isEmpty() || jwt.getIssuedAtTime() >= staleSince.get().getEpochSecond()) {
            return;
        }

        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "SESSION_STALE"))
                .build());
    }
}
