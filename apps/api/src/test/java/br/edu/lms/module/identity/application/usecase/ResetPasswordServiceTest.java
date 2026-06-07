package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.ResetPasswordCommand;
import br.edu.lms.module.identity.domain.exception.PasswordResetTokenInvalidException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.PasswordResetTokenRepository;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.PasswordHasher;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordHasher passwordHasher;

    @InjectMocks ResetPasswordService sut;

    private User activeUser(UserId id) {
        return User.builder()
                .id(id)
                .fullName(new FullName("Test User"))
                .email(new Email("user@test.com"))
                .passwordHash("old_hash")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void execute_tokenNotFound_throwsInvalidToken() {
        when(passwordResetTokenRepository.findUserId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(
                ResetPasswordCommand.builder().token("bad-token").newPassword("newpass123").build()))
                .isInstanceOf(PasswordResetTokenInvalidException.class);

        verifyNoInteractions(userRepository, refreshTokenRepository);
    }

    @Test
    void execute_validToken_updatesPasswordAndInvalidatesTokensAndSessions() {
        var userId = UserId.generate();
        var user = activeUser(userId);

        when(passwordResetTokenRepository.findUserId("valid-token"))
                .thenReturn(Optional.of(userId.getValue()));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(passwordHasher.hash("newpass123")).thenReturn("new_hash");

        sut.execute(ResetPasswordCommand.builder().token("valid-token").newPassword("newpass123").build());

        verify(userRepository).save(any());
        verify(passwordResetTokenRepository).invalidate("valid-token");
        verify(refreshTokenRepository).deleteAllByUserId(userId.getValue());
    }
}
