package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
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
    @Mock UserRepository userRepository;

    @InjectMocks RefreshTokenService sut;

    private final User user = User.builder()
            .id(UserId.of("user-1"))
            .fullName(new FullName("Ana Souza"))
            .email(new Email("ana@test.com"))
            .passwordHash("hashed")
            .status(UserStatus.ACTIVE)
            .build();

    private void givenTheUserExists() {
        when(userRepository.findById(UserId.of("user-1"))).thenReturn(Optional.of(user));
    }

    @Test
    void shouldRefreshWithoutOrgContext_noOrganization() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", null)));
        givenTheUserExists();
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1")).thenReturn(List.of());
        when(jwtTokenService.generateAccessToken(user)).thenReturn("new-access");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("new-access");
        verify(jwtTokenService, never()).generateAccessToken(any(User.class), anyString(), anyString());
        verify(refreshTokenRepository).save(anyString(), eq("user-1"), isNull(), any());
    }

    @Test
    void shouldKeepTheOrganizationTheSessionIsIn() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", "org-2")));
        givenTheUserExists();
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-2")).thenReturn(Optional.of("PROFESSOR"));
        when(jwtTokenService.generateAccessToken(user, "org-2", "PROFESSOR")).thenReturn("org-2-token");

        var result = sut.execute(new RefreshCommand("rt"));

        // A reload must not undo an organization switch.
        assertThat(result.accessToken()).isEqualTo("org-2-token");
        verify(refreshTokenRepository).save(anyString(), eq("user-1"), eq("org-2"), any());
        verify(organizationMemberLookupPort, never()).findOrganizationsByUser(anyString());
    }

    @Test
    void shouldFallBackToTheFirstOrganizationWhenTheMembershipWasRevoked() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", "org-2")));
        givenTheUserExists();
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-2")).thenReturn(Optional.empty());
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1"))
                .thenReturn(List.of(new OrgMembership("org-1", "ADMIN_ORG")));
        when(jwtTokenService.generateAccessToken(user, "org-1", "ADMIN_ORG")).thenReturn("org-1-token");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("org-1-token");
        verify(refreshTokenRepository).save(anyString(), eq("user-1"), eq("org-1"), any());
    }

    @Test
    void shouldEnterTheFirstOrganization_whenTheSessionHasNoneYet() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", null)));
        givenTheUserExists();
        when(organizationMemberLookupPort.findOrganizationsByUser("user-1")).thenReturn(List.of(
                new OrgMembership("org-1", "ADMIN_ORG"),
                new OrgMembership("org-2", "PROFESSOR")));
        when(jwtTokenService.generateAccessToken(user, "org-1", "ADMIN_ORG")).thenReturn("org-1-token");

        var result = sut.execute(new RefreshCommand("rt"));

        assertThat(result.accessToken()).isEqualTo("org-1-token");
        verify(jwtTokenService, never()).generateAccessToken(any(User.class));
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        when(refreshTokenRepository.findSession("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new RefreshCommand("bad")))
                .isInstanceOf(TokenNotFoundException.class);
    }

    @Test
    void shouldEndTheSessionWhenTheUserNoLongerExists() {
        when(refreshTokenRepository.findSession("rt")).thenReturn(Optional.of(new RefreshSession("user-1", null)));
        when(userRepository.findById(UserId.of("user-1"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new RefreshCommand("rt")))
                .isInstanceOf(TokenNotFoundException.class);
        verify(refreshTokenRepository).delete("rt");
        verify(refreshTokenRepository, never()).save(anyString(), anyString(), any(), any());
    }
}
