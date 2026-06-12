package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.domain.exception.EmailAlreadyConfirmedException;
import br.edu.lms.module.identity.domain.exception.InvalidConfirmationTokenException;
import br.edu.lms.module.identity.domain.model.UserId;
import br.edu.lms.module.identity.domain.port.in.ConfirmEmailUseCase;
import br.edu.lms.module.identity.domain.port.out.EmailConfirmationTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ConfirmEmailService implements ConfirmEmailUseCase {

    private final EmailConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void execute(String token) {
        var userId = confirmationTokenRepository.findUserId(token)
                .orElseThrow(InvalidConfirmationTokenException::new);

        var user = userRepository.findById(UserId.of(userId))
                .orElseThrow(InvalidConfirmationTokenException::new);

        if (!user.isPendingConfirmation()) {
            throw new EmailAlreadyConfirmedException();
        }

        user.activate();
        userRepository.save(user);
        confirmationTokenRepository.invalidate(token);

        log.info("Email confirmed for user: {}", userId);
    }
}
