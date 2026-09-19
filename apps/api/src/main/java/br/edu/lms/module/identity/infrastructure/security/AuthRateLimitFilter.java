package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.AuthRateLimiter;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Fecha `/auth` para a origem que erra demais. O bloqueio vale para todas as
 * rotas de autenticação, não só a que falhou: quem tenta força bruta no login
 * também não deve seguir tentando pelo "esqueci minha senha".
 *
 * <p>`refresh` e `logout` ficam de fora — são a sessão de quem já entrou, e
 * derrubá-los puniria o usuário legítimo que divide o IP com o atacante.
 *
 * <p>O 429 vem <b>antes</b> do use case: durante o bloqueio nem a senha certa
 * entra, senão bastaria acertar na tentativa seguinte para escapar do limite.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class AuthRateLimitFilter {

    private static final String AUTH_PREFIX = "auth/";
    private static final Set<String> EXEMPT = Set.of("auth/refresh", "auth/logout");
    private static final String LOGIN_PATH = "auth/login";
    private static final String RESET_PASSWORD_PATH = "auth/reset-password";
    private static final String ERROR_CODE = "AUTH_RATE_LIMIT_EXCEEDED";

    private final AuthRateLimiter rateLimiter;
    private final CurrentVertxRequest currentRequest;

    @ServerRequestFilter(priority = Priorities.AUTHENTICATION - 10)
    public Optional<Response> refuseBlockedOrigin(ContainerRequestContext requestContext) {
        if (!isRateLimited(path(requestContext))) {
            return Optional.empty();
        }

        return rateLimiter.remainingBlock(origin())
                .map(remaining -> Response.status(429)
                        // Em segundos, como manda o HTTP: o front converte para
                        // minutos na mensagem que mostra.
                        .header("Retry-After", Math.max(remaining.toSeconds(), 1))
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("error", ERROR_CODE))
                        .build());
    }

    @ServerResponseFilter
    public void countAuthenticationOutcome(ContainerRequestContext requestContext,
                                           ContainerResponseContext responseContext) {
        var path = path(requestContext);
        var status = responseContext.getStatus();

        if (LOGIN_PATH.equals(path)) {
            if (status == Response.Status.UNAUTHORIZED.getStatusCode()) {
                rateLimiter.registerFailure(origin());
            } else if (status == Response.Status.OK.getStatusCode()) {
                // Entrou: não era ataque, e o contador não pode sobreviver para
                // bloquear o usuário na próxima vez que ele errar uma vez só.
                rateLimiter.reset(origin());
            }
            return;
        }

        // Token de redefinição inválido ou expirado — a mesma tentativa cega que
        // o limite existe para conter.
        if (RESET_PASSWORD_PATH.equals(path)
                && status == Response.Status.BAD_REQUEST.getStatusCode()) {
            rateLimiter.registerFailure(origin());
        }
    }

    private static boolean isRateLimited(String path) {
        return path.startsWith(AUTH_PREFIX) && !EXEMPT.contains(path);
    }

    /** `UriInfo` devolve o caminho sem a barra inicial; normaliza para comparar. */
    private static String path(ContainerRequestContext requestContext) {
        var path = requestContext.getUriInfo().getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * Endereço remoto da conexão. Atrás de proxy ele é o do proxy: quem for
     * publicar assim precisa ligar `quarkus.http.proxy.proxy-address-forwarding`
     * para o Vert.x ler o `X-Forwarded-For` — confiar no header sem isso deixaria
     * qualquer cliente forjar a própria origem e escapar do limite.
     */
    private String origin() {
        var request = currentRequest.getCurrent();
        if (request == null || request.request().remoteAddress() == null) {
            return "unknown";
        }
        return request.request().remoteAddress().hostAddress();
    }
}
