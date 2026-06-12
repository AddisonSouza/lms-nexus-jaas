package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RegisterUserCommand;
import br.edu.lms.module.identity.domain.event.UserRegisteredEvent;
import br.edu.lms.module.identity.domain.exception.EmailAlreadyInUseException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.out.EmailConfirmationTokenRepository;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmailPort emailPort;
    @Mock BcryptPasswordService passwordService;
    @Mock EmailConfirmationTokenRepository confirmationTokenRepository;
    @Mock Event<UserRegisteredEvent> domainEvents;

    @InjectMocks RegisterUserService sut;

    private RegisterUserCommand validCommand() {
        return RegisterUserCommand.builder()
                .fullName("Test User")
                .email("user@test.com")
                .rawPassword("password123")
                .build();
    }

    @Test
    void registersUser_andPersistsConfirmationToken() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordService.hash(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(validCommand());

        // Token saved BEFORE email is sent
        var orderVerifier = inOrder(confirmationTokenRepository, emailPort);
        orderVerifier.verify(confirmationTokenRepository).save(anyString(), anyString(), eq(Duration.ofHours(24)));
        orderVerifier.verify(emailPort).sendConfirmationEmail(any(), anyString());

        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_CONFIRMATION);
    }

    @Test
    void tokenSavedMatchesTokenSentInEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordService.hash(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var savedTokenCaptor = ArgumentCaptor.forClass(String.class);
        var emailTokenCaptor = ArgumentCaptor.forClass(String.class);

        sut.execute(validCommand());

        verify(confirmationTokenRepository).save(savedTokenCaptor.capture(), anyString(), any());
        verify(emailPort).sendConfirmationEmail(any(), emailTokenCaptor.capture());

        assertThat(savedTokenCaptor.getValue()).isEqualTo(emailTokenCaptor.getValue());
    }

    @Test
    void throwsEmailAlreadyInUse_whenEmailExists() {
        var existing = User.builder()
                .id(UserId.generate())
                .fullName(new FullName("Existing"))
                .email(new Email("user@test.com"))
                .passwordHash("hash")
                .status(UserStatus.ACTIVE)
                .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(validCommand()))
                .isInstanceOf(EmailAlreadyInUseException.class);

        verifyNoInteractions(confirmationTokenRepository, emailPort);
    }
}
