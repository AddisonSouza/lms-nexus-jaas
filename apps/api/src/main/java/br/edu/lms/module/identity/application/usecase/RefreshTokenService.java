package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.port.in.RefreshTokenUseCase;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService implements RefreshTokenUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenGeneratorPort jwtTokenService;
    private final OrganizationMemberLookupPort organizationMemberLookupPort;

    @Override
    public AuthResult execute(RefreshCommand command) {
        var userId = refreshTokenRepository.findUserId(command.refreshToken())
                .orElseThrow(TokenNotFoundException::new);

        refreshTokenRepository.delete(command.refreshToken());

        var memberships = organizationMemberLookupPort.findOrganizationsByUser(userId);
        var newAccessToken = memberships.size() == 1
                ? jwtTokenService.generateAccessToken(userId, memberships.get(0).organizationId(), memberships.get(0).role())
                : jwtTokenService.generateAccessToken(userId);

        var newRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(newRefreshToken, userId, REFRESH_TOKEN_TTL);

        log.debug("Tokens rotated for user: {}", userId);

        return new AuthResult(newAccessToken, newRefreshToken);
    }
}
