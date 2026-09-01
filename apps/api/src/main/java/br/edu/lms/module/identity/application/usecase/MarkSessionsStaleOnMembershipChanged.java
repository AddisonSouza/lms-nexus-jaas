package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.domain.port.out.StaleSessionRepository;
import br.edu.lms.module.organization.domain.event.OrganizationMembershipChangedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * O papel viaja no JWT, então mudar o vínculo no banco não basta: os tokens já
 * emitidos seguiriam valendo. Quem cuida de sessão é o identity, e é aqui que
 * ele fica sabendo.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MarkSessionsStaleOnMembershipChanged {

    private final StaleSessionRepository staleSessionRepository;

    void onMembershipChanged(@Observes OrganizationMembershipChangedEvent event) {
        staleSessionRepository.markStale(event.userId());
        log.debug("Sessions of user {} marked stale after a membership change in org {}",
                event.userId(), event.organizationId());
    }
}
