package br.edu.lms.module.identity.domain.port.out;

import java.time.Duration;
import java.util.Optional;

public interface PasswordResetTokenRepository {
    void save(String token, String userId, Duration ttl);
    Optional<String> findUserId(String token);
    void invalidate(String token);
}
