package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutAndRefreshServiceTest {

    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenService jwtTokenService;
    @Mock OrganizationMemberLookupPort organizationMemberLookupPort;

    @InjectMocks LogoutService logoutSut;
    @InjectMocks RefreshTokenService refreshSut;

    @Test
    void logout_deletesToken() {
        logoutSut.execute("some-token");
        verify(refreshTokenRepository).delete("some-token");
    }

    @Test
    void refresh_validToken_rotatesAndReturnsNewPair() {
        when(refreshTokenRepository.findUserId("old-token")).thenReturn(Optional.of("user-id-123"));
        when(organizationMemberLookupPort.findOrganizationsByUser("user-id-123")).thenReturn(List.of());
        when(jwtTokenService.generateAccessToken("user-id-123")).thenReturn("new.jwt.token");

        var result = refreshSut.execute(new RefreshCommand("old-token"));

        verify(refreshTokenRepository).delete("old-token");
        verify(refreshTokenRepository).save(anyString(), eq("user-id-123"), any(), any());
        assertThat(result.accessToken()).isEqualTo("new.jwt.token");
        assertThat(result.refreshToken()).isNotBlank().isNotEqualTo("old-token");
    }

    @Test
    void refresh_expiredToken_throwsTokenNotFound() {
        when(refreshTokenRepository.findUserId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshSut.execute(new RefreshCommand("expired-token")))
                .isInstanceOf(TokenNotFoundException.class);
    }
}
