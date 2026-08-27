package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.model.RefreshSession;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.SetArgs;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
public class RefreshTokenRedisRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "rt:";
    private static final String USER_SET_PREFIX = "rt:user:";
    // Ids are UUIDs, so a colon never shows up inside either half. A value with
    // no separator is a session with no organization.
    private static final String SEPARATOR = ":";

    private final RedisDataSource redis;

    @Override
    public void save(String token, String userId, String organizationId, Duration ttl) {
        var value = organizationId == null ? userId : userId + SEPARATOR + organizationId;
        redis.value(String.class).set(KEY_PREFIX + token, value, new SetArgs().ex(ttl));
        redis.set(String.class).sadd(USER_SET_PREFIX + userId, token);
    }

    @Override
    public Optional<RefreshSession> findSession(String token) {
        return Optional.ofNullable(redis.value(String.class).get(KEY_PREFIX + token))
                .map(value -> {
                    var parts = value.split(SEPARATOR, 2);
                    return new RefreshSession(parts[0], parts.length > 1 ? parts[1] : null);
                });
    }

    @Override
    public Optional<String> findUserId(String token) {
        return findSession(token).map(RefreshSession::userId);
    }

    @Override
    public void delete(String token) {
        var userId = findUserId(token).orElse(null);
        redis.key().del(KEY_PREFIX + token);
        if (userId != null) {
            redis.set(String.class).srem(USER_SET_PREFIX + userId, token);
        }
    }

    @Override
    public void deleteAllByUserId(String userId) {
        Set<String> tokens = redis.set(String.class).smembers(USER_SET_PREFIX + userId);
        if (tokens != null && !tokens.isEmpty()) {
            var keys = tokens.stream()
                    .map(t -> KEY_PREFIX + t)
                    .toArray(String[]::new);
            redis.key().del(keys);
        }
        redis.key().del(USER_SET_PREFIX + userId);
    }
}
