package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectCode;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectRepositoryImpl implements SubjectRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public Subject save(Subject subject) {
        var entity = new SubjectJpaEntity();
        entity.setId(subject.getId().getValue());
        entity.setOrganizationId(subject.getOrganizationId());
        entity.setName(subject.getName());
        entity.setCode(subject.getCode() != null ? subject.getCode().getValue() : null);
        entity.setDescription(subject.getDescription());
        entity.setWorkloadHours(subject.getWorkloadHours());
        em.merge(entity);
        return subject;
    }

    @Override
    public Optional<Subject> findById(SubjectId id, String organizationId) {
        return Optional.ofNullable(em.find(SubjectJpaEntity.class, id.getValue()))
                .filter(e -> e.getDeletedAt() == null && e.getOrganizationId().equals(organizationId))
                .map(this::toDomain);
    }

    @Override
    public List<Subject> findAllByOrganizationId(String organizationId) {
        return em.createQuery(
                        "SELECT s FROM SubjectJpaEntity s WHERE s.organizationId = :orgId AND s.deletedAt IS NULL",
                        SubjectJpaEntity.class)
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void softDelete(SubjectId id, String organizationId) {
        em.createQuery(
                        "UPDATE SubjectJpaEntity s SET s.deletedAt = :now WHERE s.id = :id AND s.organizationId = :orgId")
                .setParameter("now", LocalDateTime.now())
                .setParameter("id", id.getValue())
                .setParameter("orgId", organizationId)
                .executeUpdate();
    }

    @Override
    public boolean existsSubjectClassroomLink(String subjectId, String classroomId) {
        var count = em.createQuery(
                        "SELECT COUNT(sc) FROM SubjectClassroomJpaEntity sc WHERE sc.id.subjectId = :sid AND sc.id.classroomId = :cid",
                        Long.class)
                .setParameter("sid", subjectId)
                .setParameter("cid", classroomId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public void saveSubjectClassroomLink(String subjectId, String classroomId) {
        var entity = new SubjectClassroomJpaEntity();
        entity.setId(new SubjectClassroomId(subjectId, classroomId));
        em.merge(entity);
    }

    @Override
    @Transactional
    public void deleteSubjectClassroomLink(String subjectId, String classroomId) {
        em.createQuery(
                        "DELETE FROM SubjectClassroomJpaEntity sc WHERE sc.id.subjectId = :sid AND sc.id.classroomId = :cid")
                .setParameter("sid", subjectId)
                .setParameter("cid", classroomId)
                .executeUpdate();
    }

    @Override
    public List<String> findClassroomIdsBySubject(String subjectId) {
        return em.createQuery(
                        "SELECT sc.id.classroomId FROM SubjectClassroomJpaEntity sc WHERE sc.id.subjectId = :sid",
                        String.class)
                .setParameter("sid", subjectId)
                .getResultList();
    }

    @Override
    public boolean existsSubjectTeacherLink(String subjectId, String memberId) {
        var count = em.createQuery(
                        "SELECT COUNT(st) FROM SubjectTeacherJpaEntity st WHERE st.id.subjectId = :sid AND st.id.memberId = :mid",
                        Long.class)
                .setParameter("sid", subjectId)
                .setParameter("mid", memberId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public void saveSubjectTeacherLink(String subjectId, String memberId) {
        var entity = new SubjectTeacherJpaEntity();
        entity.setId(new SubjectTeacherId(subjectId, memberId));
        em.merge(entity);
    }

    @Override
    @Transactional
    public void deleteSubjectTeacherLink(String subjectId, String memberId) {
        em.createQuery(
                        "DELETE FROM SubjectTeacherJpaEntity st WHERE st.id.subjectId = :sid AND st.id.memberId = :mid")
                .setParameter("sid", subjectId)
                .setParameter("mid", memberId)
                .executeUpdate();
    }

    @Override
    public List<String> findMemberIdsBySubject(String subjectId) {
        return em.createQuery(
                        "SELECT st.id.memberId FROM SubjectTeacherJpaEntity st WHERE st.id.subjectId = :sid",
                        String.class)
                .setParameter("sid", subjectId)
                .getResultList();
    }

    private Subject toDomain(SubjectJpaEntity e) {
        return Subject.builder()
                .id(SubjectId.of(e.getId()))
                .organizationId(e.getOrganizationId())
                .name(e.getName())
                .code(SubjectCode.of(e.getCode()))
                .description(e.getDescription())
                .workloadHours(e.getWorkloadHours())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
