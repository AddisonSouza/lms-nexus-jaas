package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.domain.port.in.LogoutUseCase;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void execute(String refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        log.debug("Refresh token invalidated");
    }
}
