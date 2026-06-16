package br.edu.lms.module.communication.infrastructure.cache;

import br.edu.lms.module.communication.domain.port.out.NotificationUnreadCounterPort;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class NotificationUnreadCounterRedisAdapter implements NotificationUnreadCounterPort {

    private static final String KEY_PREFIX = "communication:unread-count:";

    private final RedisDataSource redis;

    @Override
    public void increment(String userId) {
        redis.value(Long.class).incrby(KEY_PREFIX + userId, 1);
    }

    @Override
    public void decrement(String userId) {
        var key = KEY_PREFIX + userId;
        long current = get(userId);
        if (current <= 0) {
            redis.value(Long.class).set(key, 0L);
            return;
        }
        redis.value(Long.class).decrby(key, 1);
    }

    @Override
    public void reset(String userId) {
        redis.value(Long.class).set(KEY_PREFIX + userId, 0L);
    }

    @Override
    public long get(String userId) {
        Long value = redis.value(Long.class).get(KEY_PREFIX + userId);
        return value != null ? value : 0L;
    }
}
