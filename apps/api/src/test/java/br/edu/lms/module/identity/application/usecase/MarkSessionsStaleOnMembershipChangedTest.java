package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.domain.port.out.StaleSessionRepository;
import br.edu.lms.module.organization.domain.event.OrganizationMembershipChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MarkSessionsStaleOnMembershipChangedTest {

    @Mock StaleSessionRepository staleSessionRepository;

    @InjectMocks MarkSessionsStaleOnMembershipChanged sut;

    @Test
    void shouldMarkTheSessionsOfTheAffectedUser() {
        sut.onMembershipChanged(new OrganizationMembershipChangedEvent("user-1", "org-1"));

        verify(staleSessionRepository).markStale("user-1");
    }
}
