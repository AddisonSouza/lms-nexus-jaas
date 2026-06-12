package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.ResendConfirmationCommand;
import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.UserStatus;
import br.edu.lms.module.identity.domain.port.in.ResendConfirmationUseCase;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import br.edu.lms.module.identity.infrastructure.security.EmailConfirmationRedisRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResendConfirmationService implements ResendConfirmationUseCase {

    private static final Duration CONFIRMATION_TOKEN_TTL = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final EmailConfirmationRedisRepository confirmationTokenRepository;
    private final EmailPort emailPort;

    @Override
    public void execute(ResendConfirmationCommand command) {
        var email = new Email(command.getEmail());

        // Rate limit check before any lookup to avoid timing attacks
        if (confirmationTokenRepository.isRateLimited(email.getValue())) {
            log.warn("Resend rate limit exceeded for: {}", email.getValue());
            throw new br.edu.lms.module.identity.domain.exception.ResendRateLimitExceededException();
        }

        var userOpt = userRepository.findByEmail(email);

        // Do not reveal whether the email exists or account status
        if (userOpt.isEmpty() || userOpt.get().getStatus() != UserStatus.PENDING_CONFIRMATION) {
            log.debug("Resend requested for unknown or already active account: {}", email.getValue());
            return;
        }

        var user = userOpt.get();
        var token = UUID.randomUUID().toString();
        confirmationTokenRepository.save(token, user.getId().getValue(), CONFIRMATION_TOKEN_TTL);
        emailPort.sendConfirmationEmail(email, token);

        log.info("Confirmation email resent for user: {}", user.getId().getValue());
    }
}
