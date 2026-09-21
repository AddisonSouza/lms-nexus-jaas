package br.edu.lms.module.identity.domain.port.out;

import java.time.Duration;
import java.util.Optional;

/**
 * Conta as falhas de autenticação de uma origem e a bloqueia quando elas se
 * repetem — a defesa contra força bruta em `/auth`. A origem é o IP: bloquear
 * por conta deixaria o atacante trancar a conta alheia de fora.
 */
public interface AuthRateLimiter {

    /** Quanto falta do bloqueio desta origem; vazio quando ela não está bloqueada. */
    Optional<Duration> remainingBlock(String origin);

    /** Registra uma falha; ao atingir o limite dentro da janela, bloqueia a origem. */
    void registerFailure(String origin);

    /** Zera a contagem — a autenticação deu certo, então não havia ataque. */
    void reset(String origin);
}
