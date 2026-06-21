package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.application.dto.SwitchOrganizationCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.exception.UserNotMemberOfOrganizationException;
import br.edu.lms.module.identity.domain.port.in.SwitchOrganizationUseCase;
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
public class SwitchOrganizationService implements SwitchOrganizationUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenGeneratorPort jwtTokenService;
    private final OrganizationMemberLookupPort organizationMemberLookupPort;

    @Override
    public AuthResult execute(SwitchOrganizationCommand command) {
        var userId = refreshTokenRepository.findUserId(command.refreshToken())
                .orElseThrow(TokenNotFoundException::new);

        var role = organizationMemberLookupPort
                .findRoleByUserAndOrg(userId, command.organizationId())
                .orElseThrow(UserNotMemberOfOrganizationException::new);

        refreshTokenRepository.delete(command.refreshToken());

        var newAccessToken = jwtTokenService.generateAccessToken(userId, command.organizationId(), role);

        var newRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(newRefreshToken, userId, REFRESH_TOKEN_TTL);

        log.debug("Organization switched for user: {}", userId);

        return new AuthResult(newAccessToken, newRefreshToken);
    }
}
