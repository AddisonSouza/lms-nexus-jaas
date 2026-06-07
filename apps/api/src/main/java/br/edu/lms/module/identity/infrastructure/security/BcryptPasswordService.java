package br.edu.lms.module.identity.infrastructure.security;

import br.edu.lms.module.identity.domain.port.out.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@ApplicationScoped
public class BcryptPasswordService implements PasswordHasher {

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
            return new String(ModularCrypt.encode(password));
        } catch (GeneralSecurityException e) {
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
