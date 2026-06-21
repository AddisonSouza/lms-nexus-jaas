package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.model.SubjectContent;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectContentRepositoryImpl implements ContentRepository {

    private final EntityManager em;
    private final SubjectContentMapper subjectContentMapper;

    @Override
    @Transactional
    public SubjectContent save(SubjectContent content) {
        var entity = subjectContentMapper.toEntity(content);
        em.merge(entity);
        return content;
    }

    @Override
    public Optional<SubjectContent> findById(String id, String organizationId) {
        return Optional.ofNullable(em.find(SubjectContentJpaEntity.class, id))
                .filter(e -> e.getDeletedAt() == null && e.getOrganizationId().equals(organizationId))
                .map(subjectContentMapper::toDomain);
    }

    @Override
    public List<SubjectContent> findByTopicId(String topicId, String organizationId) {
        return em.createQuery(
                        "SELECT c FROM SubjectContentJpaEntity c WHERE c.topicId = :tid AND c.organizationId = :orgId AND c.deletedAt IS NULL ORDER BY c.position ASC",
                        SubjectContentJpaEntity.class)
                .setParameter("tid", topicId)
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(subjectContentMapper::toDomain)
                .toList();
    }

    @Override
    public List<SubjectContent> findBySubjectId(String subjectId, String organizationId) {
        return em.createQuery(
                        "SELECT c FROM SubjectContentJpaEntity c JOIN TopicJpaEntity t ON c.topicId = t.id WHERE t.subjectId = :sid AND c.organizationId = :orgId AND c.deletedAt IS NULL ORDER BY t.position ASC, c.position ASC",
                        SubjectContentJpaEntity.class)
                .setParameter("sid", subjectId)
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(subjectContentMapper::toDomain)
                .toList();
    }

    @Override
    public int maxPositionByTopicId(String topicId, String organizationId) {
        var result = em.createQuery(
                        "SELECT COALESCE(MAX(c.position), 0) FROM SubjectContentJpaEntity c WHERE c.topicId = :tid AND c.organizationId = :orgId AND c.deletedAt IS NULL",
                        Integer.class)
                .setParameter("tid", topicId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return result != null ? result : 0;
    }

    @Override
    @Transactional
    public void softDeleteByTopicId(String topicId, String organizationId) {
        em.createQuery(
                        "UPDATE SubjectContentJpaEntity c SET c.deletedAt = :now WHERE c.topicId = :tid AND c.organizationId = :orgId AND c.deletedAt IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("tid", topicId)
                .setParameter("orgId", organizationId)
                .executeUpdate();
    }
}
