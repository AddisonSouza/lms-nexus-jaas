package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.application.dto.RefreshCommand;
import br.edu.lms.module.identity.domain.exception.TokenNotFoundException;
import br.edu.lms.module.identity.domain.model.OrgMembership;
import br.edu.lms.module.identity.domain.port.in.RefreshTokenUseCase;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Optional;
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
        var session = refreshTokenRepository.findSession(command.refreshToken())
                .orElseThrow(TokenNotFoundException::new);
        var userId = session.userId();

        refreshTokenRepository.delete(command.refreshToken());

        // Rotation must not move the user out of the organization the session is
        // in — a page reload or an expired access token would otherwise undo an
        // organization switch. The membership is revalidated because it may have
        // been revoked mid-session; without a valid one, fall back to the rule
        // the login uses.
        var membership = currentMembership(userId, session.organizationId())
                .or(() -> firstMembership(userId));

        var organizationId = membership.map(OrgMembership::organizationId).orElse(null);
        var newAccessToken = membership
                .map(m -> jwtTokenService.generateAccessToken(userId, m.organizationId(), m.role()))
                .orElseGet(() -> jwtTokenService.generateAccessToken(userId));

        var newRefreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(newRefreshToken, userId, organizationId, REFRESH_TOKEN_TTL);

        log.debug("Tokens rotated for user: {}", userId);

        return new AuthResult(newAccessToken, newRefreshToken);
    }

    private Optional<OrgMembership> currentMembership(String userId, String organizationId) {
        if (organizationId == null) {
            return Optional.empty();
        }
        return organizationMemberLookupPort.findRoleByUserAndOrg(userId, organizationId)
                .map(role -> new OrgMembership(organizationId, role));
    }

    private Optional<OrgMembership> firstMembership(String userId) {
        return organizationMemberLookupPort.findOrganizationsByUser(userId).stream().findFirst();
    }
}
