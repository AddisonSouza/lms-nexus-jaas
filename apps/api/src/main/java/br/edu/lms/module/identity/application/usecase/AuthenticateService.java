package br.edu.lms.module.identity.application.usecase;

import br.edu.lms.module.identity.application.dto.AuthenticateCommand;
import br.edu.lms.module.identity.application.dto.AuthResult;
import br.edu.lms.module.identity.domain.exception.InvalidCredentialsException;
import br.edu.lms.module.identity.domain.model.Email;
import br.edu.lms.module.identity.domain.model.UserStatus;
import br.edu.lms.module.identity.domain.port.in.AuthenticateUseCase;
import br.edu.lms.module.identity.domain.port.out.OrganizationMemberLookupPort;
import br.edu.lms.module.identity.domain.port.out.PasswordHasher;
import br.edu.lms.module.identity.domain.port.out.RefreshTokenRepository;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import br.edu.lms.module.identity.domain.port.out.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AuthenticateService implements AuthenticateUseCase {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final PasswordHasher passwordService;
    private final TokenGeneratorPort jwtTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrganizationMemberLookupPort organizationMemberLookupPort;

    @Override
    public AuthResult execute(AuthenticateCommand command) {
        var email = new Email(command.email());

        var user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordService.verify(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }

        var userId = user.getId().getValue().toString();
        // Any membership puts an organization in the token: without one the user
        // lands on /welcome, a screen with no sidebar and no organization
        // switcher, so whoever belongs to several would never reach the app.
        // The list is ordered by organization name, so "the first" is stable.
        var memberships = organizationMemberLookupPort.findOrganizationsByUser(userId);
        var accessToken = memberships.isEmpty()
                ? jwtTokenService.generateAccessToken(userId)
                : jwtTokenService.generateAccessToken(userId, memberships.get(0).organizationId(), memberships.get(0).role());
        var refreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.save(refreshToken, userId, REFRESH_TOKEN_TTL);

        log.info("User authenticated: {}", userId);

        return new AuthResult(accessToken, refreshToken);
    }
}
