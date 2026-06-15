package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.port.out.SubjectQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectQueryAdapter implements SubjectQueryPort {

    private static final String SUBJECT_ENTITY = "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectJpaEntity";
    private static final String SUBJECT_TEACHER_ENTITY = "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectTeacherJpaEntity";

    private final EntityManager em;

    @Override
    public boolean existsByIdAndTeacher(String subjectId, String organizationId, String teacherId) {
        var count = em.createQuery(
                        "SELECT COUNT(st) FROM " + SUBJECT_TEACHER_ENTITY + " st " +
                        "JOIN " + SUBJECT_ENTITY + " s ON s.id = st.id.subjectId " +
                        "WHERE st.id.subjectId = :sid AND st.id.memberId = :tid " +
                        "AND s.organizationId = :orgId AND s.deletedAt IS NULL",
                        Long.class)
                .setParameter("sid", subjectId)
                .setParameter("tid", teacherId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public boolean existsById(String subjectId, String organizationId) {
        var count = em.createQuery(
                        "SELECT COUNT(s) FROM " + SUBJECT_ENTITY + " s " +
                        "WHERE s.id = :sid AND s.organizationId = :orgId AND s.deletedAt IS NULL",
                        Long.class)
                .setParameter("sid", subjectId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return count > 0;
    }
}
