package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ClassroomQueryPortImpl implements ClassroomQueryPort {

    private final EntityManager em;

    @Override
    public boolean existsByIdAndOrganizationId(String classroomId, String organizationId) {
        var count = em.createQuery(
                        "SELECT COUNT(c) FROM br.edu.lms.module.classroom.infrastructure.persistence.ClassroomJpaEntity c " +
                        "WHERE c.id = :id AND c.organizationId = :orgId AND c.deletedAt IS NULL",
                        Long.class)
                .setParameter("id", classroomId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean isArchived(String classroomId) {
        var count = em.createQuery(
                        "SELECT COUNT(c) FROM br.edu.lms.module.classroom.infrastructure.persistence.ClassroomJpaEntity c " +
                        "WHERE c.id = :id AND c.status = 'ARCHIVED' AND c.deletedAt IS NULL",
                        Long.class)
                .setParameter("id", classroomId)
                .getSingleResult();
        return count > 0;
    }
}
