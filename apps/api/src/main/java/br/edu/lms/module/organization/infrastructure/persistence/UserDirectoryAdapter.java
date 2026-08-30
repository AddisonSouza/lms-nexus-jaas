package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class UserDirectoryAdapter implements UserDirectoryPort {

    private static final String USER_ENTITY =
            "br.edu.lms.module.identity.infrastructure.persistence.UserJpaEntity";

    private final EntityManager entityManager;

    @Override
    public Optional<String> findEmailById(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }

        return entityManager.createQuery(
                        "SELECT u.email FROM " + USER_ENTITY + " u WHERE u.id = :id",
                        String.class)
                .setParameter("id", userId)
                .getResultStream()
                .findFirst();
    }
}
