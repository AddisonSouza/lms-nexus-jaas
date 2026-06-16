package br.edu.lms.module.identity.domain.port.out;

public interface TokenGeneratorPort {
    String generateAccessToken(String userId);
    String generateAccessToken(String userId, String orgId, String role);
}
