package br.edu.lms.module.identity.domain.port.out;

import java.time.Instant;
import java.util.Optional;

/**
 * Marca o instante a partir do qual os access tokens de um usuário deixaram de
 * refletir a realidade — o papel dele mudou, ou ele saiu da organização. Os
 * tokens emitidos antes da marca não valem mais; o refresh token continua
 * intacto, então a sessão se renova sozinha em vez de cair.
 */
public interface StaleSessionRepository {

    /** Marca agora como o instante em que as sessões do usuário ficaram obsoletas. */
    void markStale(String userId);

    /** O instante da marca, enquanto ela existir. */
    Optional<Instant> staleSince(String userId);
}
