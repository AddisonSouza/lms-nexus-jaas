package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.StaleSessionRepository;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaleSessionFilterTest {

    static final String USER_ID = "user-1";
    static final Instant MARK = Instant.parse("2026-09-01T12:00:00Z");

    @Mock StaleSessionRepository staleSessionRepository;
    @Mock ContainerRequestContext requestContext;
    @Mock SecurityContext securityContext;
    @Mock JsonWebToken jwt;

    @InjectMocks StaleSessionFilter sut;

    private void givenAToken() {
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn(USER_ID);
    }

    private void givenTokenIssuedAt(Instant issuedAt) {
        givenAToken();
        when(jwt.getIssuedAtTime()).thenReturn(issuedAt.getEpochSecond());
    }

    @Test
    void shouldRejectATokenIssuedBeforeTheMark() {
        givenTokenIssuedAt(MARK.minusSeconds(60));
        when(staleSessionRepository.staleSince(USER_ID)).thenReturn(Optional.of(MARK));

        var response = sut.refuseStaleSession(requestContext);

        assertThat(response).isPresent();
        assertThat(response.get().getStatus()).isEqualTo(401);
    }

    @Test
    void shouldLetThroughATokenIssuedAfterTheMark() {
        givenTokenIssuedAt(MARK.plusSeconds(1));
        when(staleSessionRepository.staleSince(USER_ID)).thenReturn(Optional.of(MARK));

        assertThat(sut.refuseStaleSession(requestContext)).isEmpty();
    }

    @Test
    void shouldLetThroughWhenThereIsNoMark() {
        givenAToken();
        when(staleSessionRepository.staleSince(USER_ID)).thenReturn(Optional.empty());

        assertThat(sut.refuseStaleSession(requestContext)).isEmpty();
    }

    @Test
    void shouldIgnoreARequestWithoutAJwt() {
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(mock(Principal.class));

        assertThat(sut.refuseStaleSession(requestContext)).isEmpty();
        verifyNoInteractions(staleSessionRepository);
    }

    @Test
    void shouldIgnoreAnAnonymousRequest() {
        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(null);

        assertThat(sut.refuseStaleSession(requestContext)).isEmpty();
        verifyNoInteractions(staleSessionRepository);
    }
}
