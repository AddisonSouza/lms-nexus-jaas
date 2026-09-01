package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtTokenService implements TokenGeneratorPort {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    /** Visível no pacote porque a marca de sessão obsoleta vive exatamente o mesmo tempo. */
    static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    public String generateAccessToken(String userId) {
        return Jwt.issuer(issuer)
                .subject(userId)
                .groups(Set.of())
                .issuedAt(Instant.now())
                .expiresIn(ACCESS_TOKEN_TTL)
                .sign();
    }

    public String generateAccessToken(String userId, String orgId, String role) {
        return Jwt.issuer(issuer)
                .subject(userId)
                .groups(Set.of(role))
                .claim("org", orgId)
                .issuedAt(Instant.now())
                .expiresIn(ACCESS_TOKEN_TTL)
                .sign();
    }
}
