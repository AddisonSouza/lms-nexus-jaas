package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.ResetPasswordCommand;
import br.edu.lms.module.identity.domain.exception.PasswordResetTokenInvalidException;
import br.edu.lms.module.identity.domain.model.UserId;
import br.edu.lms.module.identity.domain.port.in.ResetPasswordUseCase;
import br.edu.lms.module.identity.domain.port.out.PasswordHasher;
import br.edu.lms.module.identity.domain.port.out.PasswordResetTokenRepository;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public void execute(ResetPasswordCommand command) {
        var userId = passwordResetTokenRepository.findUserId(command.getToken())
                .orElseThrow(PasswordResetTokenInvalidException::new);

        var user = userRepository.findById(UserId.of(userId))
                .orElseThrow(PasswordResetTokenInvalidException::new);

        var newHash = passwordHasher.hash(command.getNewPassword());
        var updatedUser = user.toBuilder().passwordHash(newHash).build();

        userRepository.save(updatedUser);
        passwordResetTokenRepository.invalidate(command.getToken());
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("Password reset completed for user: {}", userId);
    }
}
