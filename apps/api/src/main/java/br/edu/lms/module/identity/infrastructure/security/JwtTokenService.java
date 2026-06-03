package br.edu.lms.module.identity.infrastructure.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtTokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);

    public String generateAccessToken(String userId) {
        return Jwt.issuer(issuer)
                .subject(userId)
                .groups(Set.of())
                .issuedAt(Instant.now())
                .expiresIn(ACCESS_TOKEN_TTL)
                .sign();
    }
}
