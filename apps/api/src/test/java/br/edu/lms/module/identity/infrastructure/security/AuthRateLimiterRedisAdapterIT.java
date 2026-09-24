package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.AuthRateLimiter;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contagem e bloqueio por origem no Redis: o limitador só bloqueia ao atingir o
 * limite, e o bloqueio carrega quanto tempo ainda falta.
 */
@QuarkusTest
class AuthRateLimiterRedisAdapterIT {

    static final String ORIGIN = "203.0.113.7";
    static final String OTHER_ORIGIN = "203.0.113.8";

    @Inject AuthRateLimiter rateLimiter;
    @Inject RedisDataSource redis;

    @ConfigProperty(name = "lms.auth.rate-limit.max-failures") int maxFailures;
    @ConfigProperty(name = "lms.auth.rate-limit.block-seconds") long blockSeconds;

    /** Logger do adapter, visto pelo JUL: o JBoss LogManager do Quarkus o estende. */
    private final Logger adapterLogger = Logger.getLogger(AuthRateLimiterRedisAdapter.class.getName());
    private final List<LogRecord> warnings = new CopyOnWriteArrayList<>();
    private final Handler warningCollector = new Handler() {
        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                warnings.add(record);
            }
        }

        @Override public void flush() { }
        @Override public void close() { }
    };

    private void clearKeys() {
        for (var origin : new String[]{ORIGIN, OTHER_ORIGIN}) {
            redis.key().del("auth-rl:fail:" + origin, "auth-rl:block:" + origin);
        }
    }

    @BeforeEach
    void setUp() {
        clearKeys();
        adapterLogger.addHandler(warningCollector);
    }

    @AfterEach
    void tearDown() {
        adapterLogger.removeHandler(warningCollector);
        clearKeys();
    }

    @Test
    void unknownOrigin_isNotBlocked() {
        assertTrue(rateLimiter.remainingBlock(ORIGIN).isEmpty());
    }

    @Test
    void unknownOrigin_isNotReportedAsAnOutage() {
        rateLimiter.remainingBlock(ORIGIN);

        // Origem sem bloqueio é o caso de todo login: um warn aqui enche o log de
        // falsos "Redis fora" e esconde a queda de verdade.
        assertTrue(warnings.isEmpty(), () -> "warn inesperado: " + warnings.get(0).getMessage());
    }

    @Test
    void failuresBelowTheLimit_doNotBlock() {
        for (int i = 0; i < maxFailures - 1; i++) {
            rateLimiter.registerFailure(ORIGIN);
        }

        assertTrue(rateLimiter.remainingBlock(ORIGIN).isEmpty());
    }

    @Test
    void reachingTheLimit_blocksWithTheRemainingTime() {
        for (int i = 0; i < maxFailures; i++) {
            rateLimiter.registerFailure(ORIGIN);
        }

        var remaining = rateLimiter.remainingBlock(ORIGIN);
        assertTrue(remaining.isPresent());
        assertTrue(remaining.get().compareTo(Duration.ofSeconds(blockSeconds)) <= 0);
        assertTrue(remaining.get().toSeconds() > 0);
    }

    @Test
    void blockingOneOrigin_leavesTheOthersAlone() {
        for (int i = 0; i < maxFailures; i++) {
            rateLimiter.registerFailure(ORIGIN);
        }

        assertTrue(rateLimiter.remainingBlock(ORIGIN).isPresent());
        assertTrue(rateLimiter.remainingBlock(OTHER_ORIGIN).isEmpty());
    }

    @Test
    void reset_clearsTheFailuresCountedSoFar() {
        for (int i = 0; i < maxFailures - 1; i++) {
            rateLimiter.registerFailure(ORIGIN);
        }
        rateLimiter.reset(ORIGIN);

        // A contagem recomeçou: a falha seguinte é a primeira, não a última
        // antes do bloqueio.
        rateLimiter.registerFailure(ORIGIN);

        assertTrue(rateLimiter.remainingBlock(ORIGIN).isEmpty());
    }

    @Test
    void failureCount_expiresWithTheWindow() {
        rateLimiter.registerFailure(ORIGIN);

        long ttl = redis.key().ttl("auth-rl:fail:" + ORIGIN);
        assertTrue(ttl > 0, "a janela precisa expirar sozinha, senão as falhas acumulam para sempre");
    }
}
