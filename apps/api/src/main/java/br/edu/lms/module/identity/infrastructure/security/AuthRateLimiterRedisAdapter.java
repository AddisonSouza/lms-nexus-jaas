package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.AuthRateLimiter;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Optional;

/**
 * Contagem de falhas e bloqueio no Redis, no mesmo padrão dos demais
 * repositórios de `identity`. Duas chaves por origem: `auth-rl:fail:{origem}`,
 * que expira sozinha e implementa a janela, e `auth-rl:block:{origem}`, cujo TTL
 * é o tempo restante do bloqueio.
 *
 * <p><b>Fail-open de propósito.</b> Se o Redis cair, o serviço deixa passar e
 * registra um warn: derrubar o login inteiro por causa do limitador trocaria
 * uma brecha de força bruta por uma indisponibilidade total.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AuthRateLimiterRedisAdapter implements AuthRateLimiter {

    private static final String FAILURE_PREFIX = "auth-rl:fail:";
    private static final String BLOCK_PREFIX = "auth-rl:block:";
    /** Valor irrelevante: o que importa da chave de bloqueio é existir e ter TTL. */
    private static final String BLOCKED = "1";

    private final RedisDataSource redis;

    @ConfigProperty(name = "lms.auth.rate-limit.max-failures", defaultValue = "5")
    int maxFailures;

    @ConfigProperty(name = "lms.auth.rate-limit.window-seconds", defaultValue = "60")
    long windowSeconds;

    @ConfigProperty(name = "lms.auth.rate-limit.block-seconds", defaultValue = "900")
    long blockSeconds;

    @Override
    public Optional<Duration> remainingBlock(String origin) {
        try {
            long ttl = redis.key().ttl(BLOCK_PREFIX + origin);
            // -2 = chave não existe, -1 = existe sem TTL (não acontece aqui).
            return ttl > 0 ? Optional.of(Duration.ofSeconds(ttl)) : Optional.empty();
        } catch (RuntimeException e) {
            log.warn("Rate limiter unavailable, letting the request through", e);
            return Optional.empty();
        }
    }

    @Override
    public void registerFailure(String origin) {
        try {
            String failureKey = FAILURE_PREFIX + origin;
            long failures = redis.value(Long.class).incr(failureKey);

            // A janela começa na primeira falha e não é estendida pelas
            // seguintes: quem erra devagar nunca acumula até o limite.
            if (failures == 1) {
                redis.key().expire(failureKey, Duration.ofSeconds(windowSeconds));
            }

            if (failures >= maxFailures) {
                redis.value(String.class).set(BLOCK_PREFIX + origin, BLOCKED,
                        new SetArgs().ex(Duration.ofSeconds(blockSeconds)));
                redis.key().del(failureKey);
                log.warn("Authentication blocked for {} after {} failures", origin, failures);
            }
        } catch (RuntimeException e) {
            log.warn("Rate limiter unavailable, failure not counted", e);
        }
    }

    @Override
    public void reset(String origin) {
        try {
            redis.key().del(FAILURE_PREFIX + origin);
        } catch (RuntimeException e) {
            log.warn("Rate limiter unavailable, failure count not reset", e);
        }
    }
}
