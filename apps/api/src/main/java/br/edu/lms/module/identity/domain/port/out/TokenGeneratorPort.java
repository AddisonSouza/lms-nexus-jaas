package br.edu.lms.module.identity.domain.port.out;

import br.edu.lms.module.identity.domain.model.User;

public interface TokenGeneratorPort {
    String generateAccessToken(User user);
    String generateAccessToken(User user, String orgId, String role);
}
