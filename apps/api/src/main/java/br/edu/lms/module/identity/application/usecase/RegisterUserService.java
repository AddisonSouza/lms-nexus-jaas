package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RegisterUserCommand;
import br.edu.lms.module.identity.application.dto.RegisterUserResponse;
import br.edu.lms.module.identity.domain.event.UserRegisteredEvent;
import br.edu.lms.module.identity.domain.exception.EmailAlreadyInUseException;
import br.edu.lms.module.identity.domain.model.*;
import br.edu.lms.module.identity.domain.port.in.RegisterUserUseCase;
import br.edu.lms.module.identity.domain.port.out.EmailConfirmationTokenRepository;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import br.edu.lms.module.identity.infrastructure.security.BcryptPasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RegisterUserService implements RegisterUserUseCase {

    private static final Duration CONFIRMATION_TOKEN_TTL = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final EmailPort emailPort;
    private final BcryptPasswordService passwordService;
    private final EmailConfirmationTokenRepository confirmationTokenRepository;
    private final Event<UserRegisteredEvent> domainEvents;

    @Override
    public RegisterUserResponse execute(RegisterUserCommand command) {
        var email = new Email(command.email());

        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new EmailAlreadyInUseException(email.getValue());
        });

        var user = User.builder()
                .id(UserId.generate())
                .fullName(new FullName(command.fullName()))
                .email(email)
                .passwordHash(passwordService.hash(command.rawPassword()))
                .status(UserStatus.PENDING_CONFIRMATION)
                .build();

        userRepository.save(user);

        var token = UUID.randomUUID().toString();
        confirmationTokenRepository.save(token, user.getId().getValue(), CONFIRMATION_TOKEN_TTL);
        emailPort.sendConfirmationEmail(email, token);

        domainEvents.fireAsync(new UserRegisteredEvent(user.getId(), email));

        log.info("User registered: {}", user.getId().getValue());

        return RegisterUserResponse.builder()
                .userId(user.getId().getValue())
                .email(email.getValue())
                .status(user.getStatus())
                .build();
    }
}
