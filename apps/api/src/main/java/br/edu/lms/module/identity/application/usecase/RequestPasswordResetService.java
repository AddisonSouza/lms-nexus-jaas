package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.RequestPasswordResetCommand;
import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.UserStatus;
import br.edu.lms.module.identity.domain.port.in.RequestPasswordResetUseCase;
import br.edu.lms.module.identity.domain.port.out.EmailPort;
import br.edu.lms.module.identity.domain.port.out.PasswordResetTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailPort emailPort;

    @Override
    public void execute(RequestPasswordResetCommand command) {
        var email = new Email(command.getEmail());

        var userOpt = userRepository.findByEmail(email);

        // Do not reveal whether the email exists (SEC-06 / RF-03 requirement)
        if (userOpt.isEmpty() || userOpt.get().getStatus() != UserStatus.ACTIVE) {
            log.debug("Password reset requested for unknown or inactive account: {}", command.getEmail());
            return;
        }

        var user = userOpt.get();
        var token = UUID.randomUUID().toString();

        passwordResetTokenRepository.save(token, user.getId().getValue(), RESET_TOKEN_TTL);
        emailPort.sendPasswordResetEmail(email, token);

        log.info("Password reset token issued for user: {}", user.getId().getValue());
    }
}
