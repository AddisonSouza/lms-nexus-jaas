package br.edu.lms.module.classroom.infrastructure.persistence;

import br.edu.lms.module.classroom.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class UserDirectoryAdapter implements UserDirectoryPort {

    private static final String USER_ENTITY =
            "br.edu.lms.module.identity.infrastructure.persistence.UserJpaEntity";

    private final EntityManager entityManager;

    @Override
    public Map<String, String> findNamesByIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        var rows = entityManager.createQuery(
                        "SELECT u.id, u.fullName FROM " + USER_ENTITY + " u WHERE u.id IN :ids",
                        Tuple.class)
                .setParameter("ids", userIds)
                .getResultList();

        Map<String, String> names = new HashMap<>();
        for (Tuple row : rows) {
            names.put(row.get(0, String.class), row.get(1, String.class));
        }
        return names;
    }
}
