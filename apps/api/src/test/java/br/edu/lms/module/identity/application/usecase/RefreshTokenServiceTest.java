package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.exception.UserNotMemberOfOrganizationException;
import br.edu.lms.module.identity.domain.model.OrgMembership;
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
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1")).thenReturn(List.of());
        when(jwtTokenService.generateAccessToken("user-1")).thenReturn("new-access");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("new-access");
        verify(jwtTokenService, never()).generateAccessToken(anyString(), anyString(), anyString());
    }

    @Test
    void shouldRefreshWithoutOrgContext_exactlyOneOrganization_resolvesAutomatically() {
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1"))
                .thenReturn(List.of(new OrgMembership("org-1", "ADMIN_ORG")));
        when(jwtTokenService.generateAccessToken("user-1", "org-1", "ADMIN_ORG")).thenReturn("org-scoped-token");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("org-scoped-token");
        verify(jwtTokenService, never()).generateAccessToken(anyString());
    }

    @Test
    void shouldRefreshWithoutOrgContext_multipleOrganizations_keepsClaimsAbsent() {
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1")).thenReturn(List.of(
                new OrgMembership("org-1", "ADMIN_ORG"),
                new OrgMembership("org-2", "PROFESSOR")));
        when(jwtTokenService.generateAccessToken("user-1")).thenReturn("new-access");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("new-access");
        verify(jwtTokenService, never()).generateAccessToken(anyString(), anyString(), anyString());
    }

    @Test
    void shouldRefreshWithOrgContextWhenMember() {
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-1"))
                .thenReturn(Optional.of("ADMIN_ORG"));
        when(jwtTokenService.generateAccessToken("user-1", "org-1", "ADMIN_ORG")).thenReturn("org-access");

        var result = sut.execute(new RefreshCommand("rt", "org-1"));

        assertThat(result.accessToken()).isEqualTo("org-access");
    }

    @Test
    void shouldThrowWhenUserNotMemberOfOrg() {
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-x"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new RefreshCommand("rt", "org-x")))
                .isInstanceOf(UserNotMemberOfOrganizationException.class);
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        when(refreshTokenRepository.findUserId("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new RefreshCommand("bad")))
                .isInstanceOf(TokenNotFoundException.class);
    }
}
