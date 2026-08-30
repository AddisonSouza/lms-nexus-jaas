package br.edu.lms.module.organization.infrastructure.persistence;

import br.edu.lms.module.organization.domain.model.UserProfile;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    @Override
    public Map<String, UserProfile> findProfilesByIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        var rows = entityManager.createQuery(
                        "SELECT u.id, u.fullName, u.email FROM " + USER_ENTITY + " u WHERE u.id IN :ids",
                        Tuple.class)
                .setParameter("ids", userIds)
                .getResultList();

        Map<String, UserProfile> profiles = new HashMap<>();
        for (Tuple row : rows) {
            var id = row.get(0, String.class);
            profiles.put(id, new UserProfile(id, row.get(1, String.class), row.get(2, String.class)));
        }
        return profiles;
    }
}
