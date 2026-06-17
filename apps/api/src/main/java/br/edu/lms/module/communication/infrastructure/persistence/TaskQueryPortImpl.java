package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.port.out.TaskQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class TaskQueryPortImpl implements TaskQueryPort {

    private final EntityManager em;

    @Override
    public Optional<String> findSubjectIdByTask(String taskId) {
        try {
            var subjectId = em.createQuery(
                            "SELECT t.subjectId FROM br.edu.lms.module.assessment.infrastructure.persistence.TaskJpaEntity t " +
                                    "WHERE t.id = :tid",
                            String.class)
                    .setParameter("tid", taskId)
                    .getSingleResult();
            return Optional.ofNullable(subjectId);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
