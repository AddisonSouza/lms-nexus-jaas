package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ClassroomQueryPortImpl implements ClassroomQueryPort {

    private final EntityManager em;

    @Override
    public boolean isMember(String userId, String classroomId, String organizationId, String role) {
        var jpql = new StringBuilder(
                "SELECT COUNT(m) FROM br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity m " +
                        "WHERE m.userId = :uid AND m.classroomId = :cid AND m.organizationId = :orgId AND m.deletedAt IS NULL");
        if (role != null) {
            jpql.append(" AND m.role = :role");
        }

        var query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("uid", userId)
                .setParameter("cid", classroomId)
                .setParameter("orgId", organizationId);
        if (role != null) {
            query.setParameter("role", role);
        }

        return query.getSingleResult() > 0;
    }

    @Override
    public List<String> listMemberUserIds(String classroomId, String role) {
        var jpql = new StringBuilder(
                "SELECT m.userId FROM br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity m " +
                        "WHERE m.classroomId = :cid AND m.deletedAt IS NULL");
        if (role != null) {
            jpql.append(" AND m.role = :role");
        }

        var query = em.createQuery(jpql.toString(), String.class)
                .setParameter("cid", classroomId);
        if (role != null) {
            query.setParameter("role", role);
        }

        return query.getResultList();
    }
}
