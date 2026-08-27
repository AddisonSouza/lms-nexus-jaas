package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.model.OrgMembership;
import br.edu.lms.module.identity.domain.model.RefreshSession;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock TokenGeneratorPort jwtTokenService;
    @Mock OrganizationMemberLookupPort organizationMemberLookupPort;

    @InjectMocks RefreshTokenService sut;

    @Test
    void shouldRefreshWithoutOrgContext_noOrganization() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", null)));
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1")).thenReturn(List.of());
        when(jwtTokenService.generateAccessToken("user-1")).thenReturn("new-access");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("new-access");
        verify(jwtTokenService, never()).generateAccessToken(anyString(), anyString(), anyString());
        verify(refreshTokenRepository).save(anyString(), eq("user-1"), isNull(), any());
    }

    @Test
    void shouldKeepTheOrganizationTheSessionIsIn() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", "org-2")));
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-2")).thenReturn(Optional.of("PROFESSOR"));
        when(jwtTokenService.generateAccessToken("user-1", "org-2", "PROFESSOR")).thenReturn("org-2-token");

        var result = sut.execute(new RefreshCommand("rt"));

        // A reload must not undo an organization switch.
        assertThat(result.accessToken()).isEqualTo("org-2-token");
        verify(refreshTokenRepository).save(anyString(), eq("user-1"), eq("org-2"), any());
        verify(organizationMemberLookupPort, never()).findOrganizationsByUser(anyString());
    }

    @Test
    void shouldFallBackToTheFirstOrganizationWhenTheMembershipWasRevoked() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", "org-2")));
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-2")).thenReturn(Optional.empty());
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1"))
                .thenReturn(List.of(new OrgMembership("org-1", "ADMIN_ORG")));
        when(jwtTokenService.generateAccessToken("user-1", "org-1", "ADMIN_ORG")).thenReturn("org-1-token");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("org-1-token");
        verify(refreshTokenRepository).save(anyString(), eq("user-1"), eq("org-1"), any());
    }

    @Test
    void shouldEnterTheFirstOrganization_whenTheSessionHasNoneYet() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", null)));
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1")).thenReturn(List.of(
                new OrgMembership("org-1", "ADMIN_ORG"),
                new OrgMembership("org-2", "PROFESSOR")));
        when(jwtTokenService.generateAccessToken("user-1", "org-1", "ADMIN_ORG")).thenReturn("org-1-token");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("org-1-token");
        verify(jwtTokenService, never()).generateAccessToken(anyString());
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        when(refreshTokenRepository.findSession("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new RefreshCommand("bad")))
                .isInstanceOf(TokenNotFoundException.class);
    }
}
