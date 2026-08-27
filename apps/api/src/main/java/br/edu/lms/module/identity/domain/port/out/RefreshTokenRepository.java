package br.edu.lms.module.identity.domain.port.out;

import br.edu.lms.module.identity.domain.model.RefreshSession;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenRepository {
    void save(String token, String userId, String organizationId, Duration ttl);
    Optional<RefreshSession> findSession(String token);
    Optional<String> findUserId(String token);
    void delete(String token);
    void deleteAllByUserId(String userId);
}
