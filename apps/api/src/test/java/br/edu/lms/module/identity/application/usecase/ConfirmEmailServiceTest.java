package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.domain.exception.EmailAlreadyConfirmedException;
import br.edu.lms.module.identity.domain.exception.InvalidConfirmationTokenException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.EmailConfirmationTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmEmailServiceTest {

    @Mock EmailConfirmationTokenRepository confirmationTokenRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ConfirmEmailService sut;

    private static final String TOKEN = "valid-token-uuid";

    private User pendingUser() {
        return User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Test User"))
                .email(new Email("user@test.com"))
                .passwordHash("hashed")
                .status(UserStatus.PENDING_CONFIRMATION)
                .build();
    }

    @Test
    void confirmsEmail_whenTokenValidAndUserPending() {
        var user = pendingUser();
        when(confirmationTokenRepository.findUserId(TOKEN))
                .thenReturn(Optional.of(user.getId().getValue()));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        sut.execute(TOKEN);

        verify(userRepository).save(user);
        verify(confirmationTokenRepository).invalidate(TOKEN);
    }

    @Test
    void throwsInvalidToken_whenTokenNotFoundInRedis() {
        when(confirmationTokenRepository.findUserId(TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(TOKEN))
                .isInstanceOf(InvalidConfirmationTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void throwsAlreadyConfirmed_whenUserIsActive() {
        var user = User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Test User"))
                .email(new Email("user@test.com"))
                .passwordHash("hashed")
                .status(UserStatus.ACTIVE)
                .build();

        when(confirmationTokenRepository.findUserId(TOKEN))
                .thenReturn(Optional.of(user.getId().getValue()));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sut.execute(TOKEN))
                .isInstanceOf(EmailAlreadyConfirmedException.class);

        verify(userRepository, never()).save(any());
    }
}
