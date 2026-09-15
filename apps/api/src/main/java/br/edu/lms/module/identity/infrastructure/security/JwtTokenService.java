package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.model.User;
import br.edu.lms.module.identity.domain.port.out.TokenGeneratorPort;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
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

    public String generateAccessToken(User user) {
        return claimsFor(user)
                .groups(Set.of())
                .sign();
    }

    public String generateAccessToken(User user, String orgId, String role) {
        return claimsFor(user)
                .groups(Set.of(role))
                .claim("org", orgId)
                .sign();
    }

    /** O front exibe `name` (ou `email`) no header — sem elas só restaria o UUID do `sub`. */
    private JwtClaimsBuilder claimsFor(User user) {
        return Jwt.issuer(issuer)
                .subject(user.getId().getValue())
                .claim("name", user.getFullName().getValue())
                .claim("email", user.getEmail().getValue())
                .issuedAt(Instant.now())
                .expiresIn(ACCESS_TOKEN_TTL);
    }
}
