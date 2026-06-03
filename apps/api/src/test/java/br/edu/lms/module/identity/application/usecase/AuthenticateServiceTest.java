package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.AuthenticateCommand;
import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.domain.exception.InvalidCredentialsException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import br.edu.lms.module.identity.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateServiceTest {

    @Mock UserRepository userRepository;
    @Mock BcryptPasswordService passwordService;
    @Mock JwtTokenService jwtTokenService;
    @Mock RefreshTokenRepository refreshTokenRepository;

    @InjectMocks AuthenticateService sut;

    private User activeUser() {
        return User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Test User"))
                .email(new Email("user@test.com"))
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_validCredentials_returnsTokens() {
        var user = activeUser();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordService.verify("secret", "hashed")).thenReturn(true);
        when(jwtTokenService.generateAccessToken(any())).thenReturn("jwt.token.here");

        AuthResult result = sut.execute(new AuthenticateCommand("user@test.com", "secret"));

        assertThat(result.accessToken()).isEqualTo("jwt.token.here");
        assertThat(result.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).save(anyString(), anyString(), any());
    }

    @Test
    void execute_emailNotFound_throwsInvalidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(new AuthenticateCommand("x@test.com", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_wrongPassword_throwsInvalidCredentials() {
        var user = activeUser();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordService.verify(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> sut.execute(new AuthenticateCommand("user@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void execute_pendingUser_throwsInvalidCredentials() {
        var user = User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Test"))
                .email(new Email("user@test.com"))
                .passwordHash("hashed")
                .status(UserStatus.PENDING_CONFIRMATION)
                .build();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(passwordService.verify(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(new AuthenticateCommand("user@test.com", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
