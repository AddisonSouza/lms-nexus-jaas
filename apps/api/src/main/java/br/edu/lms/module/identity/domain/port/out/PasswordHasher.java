package br.edu.lms.module.identity.domain.port.out;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean verify(String rawPassword, String hash);
}
