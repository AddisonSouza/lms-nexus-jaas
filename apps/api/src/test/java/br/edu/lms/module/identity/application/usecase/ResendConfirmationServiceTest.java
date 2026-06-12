package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.ResendConfirmationCommand;
import br.edu.lms.module.identity.domain.exception.ResendRateLimitExceededException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import br.edu.lms.module.identity.infrastructure.security.EmailConfirmationRedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendConfirmationServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmailConfirmationRedisRepository confirmationTokenRepository;
    @Mock EmailPort emailPort;

    @InjectMocks ResendConfirmationService sut;

    private ResendConfirmationCommand cmd(String email) {
        return ResendConfirmationCommand.builder().email(email).build();
    }

    private User pendingUser(String email) {
        return User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Test"))
                .email(new Email(email))
                .passwordHash("hashed")
                .status(UserStatus.PENDING_CONFIRMATION)
                .build();
    }

    @Test
    void resendsEmail_whenWithinRateLimit() {
        var email = "user@test.com";
        var user = pendingUser(email);
        when(confirmationTokenRepository.isRateLimited(email)).thenReturn(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        sut.execute(cmd(email));

        verify(confirmationTokenRepository).save(anyString(), eq(user.getId().getValue()), any());
        verify(emailPort).sendConfirmationEmail(any(), anyString());
    }

    @Test
    void throwsRateLimit_whenLimitExceeded() {
        var email = "user@test.com";
        when(confirmationTokenRepository.isRateLimited(email)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(cmd(email)))
                .isInstanceOf(ResendRateLimitExceededException.class);

        verify(emailPort, never()).sendConfirmationEmail(any(), any());
    }

    @Test
    void doesNothing_whenEmailNotFound() {
        var email = "unknown@test.com";
        when(confirmationTokenRepository.isRateLimited(email)).thenReturn(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        sut.execute(cmd(email));

        verify(emailPort, never()).sendConfirmationEmail(any(), any());
    }

    @Test
    void doesNothing_whenAccountAlreadyActive() {
        var email = "active@test.com";
        var user = User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Active"))
                .email(new Email(email))
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .build();
        when(confirmationTokenRepository.isRateLimited(email)).thenReturn(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        sut.execute(cmd(email));

        verify(emailPort, never()).sendConfirmationEmail(any(), any());
    }
}
