package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.StaleSessionRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class StaleSessionRedisRepository implements StaleSessionRepository {

    private static final String KEY_PREFIX = "identity:stale-since:";

    private final RedisDataSource redis;

    @Override
    public void markStale(String userId) {
        // O TTL é o do access token: passado ele, nenhum token anterior à marca
        // ainda vive, e a chave some sozinha em vez de ser lida para sempre.
        redis.value(Long.class).set(
                KEY_PREFIX + userId,
                Instant.now().getEpochSecond(),
                new SetArgs().ex(JwtTokenService.ACCESS_TOKEN_TTL));
    }

    @Override
    public Optional<Instant> staleSince(String userId) {
        return Optional.ofNullable(redis.value(Long.class).get(KEY_PREFIX + userId))
                .map(Instant::ofEpochSecond);
    }
}
