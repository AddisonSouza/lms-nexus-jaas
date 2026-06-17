package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.port.out.SubjectQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectQueryPortImpl implements SubjectQueryPort {

    private final EntityManager em;

    @Override
    public List<String> findClassroomIdsBySubject(String subjectId) {
        return em.createQuery(
                        "SELECT sc.id.classroomId FROM br.edu.lms.module.curriculum.infrastructure.persistence.SubjectClassroomJpaEntity sc " +
                                "WHERE sc.id.subjectId = :sid",
                        String.class)
                .setParameter("sid", subjectId)
                .getResultList();
    }

    @Override
    public List<String> findTeacherUserIdsBySubject(String subjectId) {
        return em.createQuery(
                        "SELECT om.userId FROM br.edu.lms.module.curriculum.infrastructure.persistence.SubjectTeacherJpaEntity st " +
                                "JOIN br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity om " +
                                "ON om.id = st.id.memberId " +
                                "WHERE st.id.subjectId = :sid AND om.deletedAt IS NULL",
                        String.class)
                .setParameter("sid", subjectId)
                .getResultList();
    }
}
