package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RequestPasswordResetCommand;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import br.edu.lms.module.identity.domain.port.out.PasswordResetTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock EmailPort emailPort;

    @InjectMocks RequestPasswordResetService sut;

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
    void execute_emailNotFound_returnsWithoutSideEffects() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        sut.execute(RequestPasswordResetCommand.builder().email("unknown@test.com").build());

        verifyNoInteractions(passwordResetTokenRepository, emailPort);
    }

    @Test
    void execute_pendingUser_returnsWithoutSideEffects() {
        var pending = User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Pending"))
                .email(new Email("pending@test.com"))
                .passwordHash("hash")
                .status(UserStatus.PENDING_CONFIRMATION)
                .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(pending));

        sut.execute(RequestPasswordResetCommand.builder().email("pending@test.com").build());

        verifyNoInteractions(passwordResetTokenRepository, emailPort);
    }

    @Test
    void execute_activeUser_savesTokenAndSendsEmail() {
        var user = activeUser();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        sut.execute(RequestPasswordResetCommand.builder().email("user@test.com").build());

        verify(passwordResetTokenRepository).save(anyString(), eq(user.getId().getValue()), any());
        verify(emailPort).sendPasswordResetEmail(any(), anyString());
    }
}
