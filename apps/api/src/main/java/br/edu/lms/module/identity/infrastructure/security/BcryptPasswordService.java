package br.edu.lms.module.identity.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

@ApplicationScoped
public class BcryptPasswordService {

    @ConfigProperty(name = "lms.security.bcrypt.cost", defaultValue = "12")
    int cost;

    public String hash(String rawPassword) {
        try {
            var factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
            var salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
            new SecureRandom().nextBytes(salt);
            var spec = new EncryptablePasswordSpec(
                    rawPassword.toCharArray(),
                    new IteratedSaltedPasswordAlgorithmSpec(cost, salt));
            Password password = factory.generatePassword(spec);
            return new String(factory.getKeySpec(password,
                    org.wildfly.security.password.spec.HashPasswordSpec.class).getHashedPassword());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("BCrypt hash failed", e);
        }
    }

    public boolean verify(String rawPassword, String hash) {
        try {
            var factory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT);
            var password = factory.translate(
                    org.wildfly.security.password.util.ModularCrypt.decode(hash));
            return factory.verify(password, rawPassword.toCharArray());
        } catch (Exception e) {
            return false;
        }
    }
}
