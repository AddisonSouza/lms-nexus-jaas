package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.SwitchOrganizationCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.exception.UserNotMemberOfOrganizationException;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwitchOrganizationServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock TokenGeneratorPort jwtTokenService;
    @Mock OrganizationMemberLookupPort organizationMemberLookupPort;

    @InjectMocks SwitchOrganizationService sut;

    @Test
    void shouldSwitchOrganizationWhenMember() {
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-1"))
                .thenReturn(Optional.of("ADMIN_ORG"));
        when(jwtTokenService.generateAccessToken("user-1", "org-1", "ADMIN_ORG")).thenReturn("org-access");

        var result = sut.execute(new SwitchOrganizationCommand("rt", "org-1"));

        assertThat(result.accessToken()).isEqualTo("org-access");
        verify(refreshTokenRepository).delete("rt");
    }

    @Test
    void shouldThrowWhenUserNotMemberOfOrg() {
        when(refreshTokenRepository.findUserId("rt")).thenReturn(Optional.of("user-1"));
        when(organizationMemberLookupPort.findRoleByUserAndOrg("user-1", "org-x"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new SwitchOrganizationCommand("rt", "org-x")))
                .isInstanceOf(UserNotMemberOfOrganizationException.class);
    }

    @Test
    void shouldThrowWhenRefreshTokenInvalid() {
        when(refreshTokenRepository.findUserId("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new SwitchOrganizationCommand("bad", "org-1")))
                .isInstanceOf(TokenNotFoundException.class);
    }
}
