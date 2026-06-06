package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class RefreshTokenRedisRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "rt:";

    private final RedisDataSource redis;

    @Override
    public void save(String token, String userId, Duration ttl) {
        redis.value(String.class).set(KEY_PREFIX + token, userId,
                new SetArgs().ex(ttl));
    }

    @Override
    public Optional<String> findUserId(String token) {
        return Optional.ofNullable(redis.value(String.class).get(KEY_PREFIX + token));
    }

    @Override
    public void delete(String token) {
        redis.key().del(KEY_PREFIX + token);
    }
}
