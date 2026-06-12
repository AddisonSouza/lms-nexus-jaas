package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.EmailConfirmationTokenRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class EmailConfirmationRedisRepository implements EmailConfirmationTokenRepository {

    private static final String TOKEN_PREFIX = "ect:";
    private static final String RATE_LIMIT_PREFIX = "ect-rl:";
    private static final int MAX_RESENDS_PER_HOUR = 3;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofHours(1);

    private final RedisDataSource redis;

    @Override
    public void save(String token, String userId, Duration ttl) {
        redis.value(String.class).set(TOKEN_PREFIX + token, userId,
                new SetArgs().ex(ttl));
    }

    @Override
    public Optional<String> findUserId(String token) {
        return Optional.ofNullable(redis.value(String.class).get(TOKEN_PREFIX + token));
    }

    @Override
    public void invalidate(String token) {
        redis.key().del(TOKEN_PREFIX + token);
    }

    public boolean isRateLimited(String email) {
        String key = RATE_LIMIT_PREFIX + email;
        Long count = redis.value(Long.class).incr(key);
        if (count == 1) {
            redis.key().expire(key, RATE_LIMIT_WINDOW);
        }
        return count > MAX_RESENDS_PER_HOUR;
    }
}
