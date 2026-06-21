package br.edu.lms.module.curriculum.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.model.Topic;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class TopicRepositoryImpl implements TopicRepository {

    private final EntityManager em;
    private final TopicMapper topicMapper;

    @Override
    @Transactional
    public Topic save(Topic topic) {
        var entity = topicMapper.toEntity(topic);
        em.merge(entity);
        return topic;
    }

    @Override
    public Optional<Topic> findById(String id, String organizationId) {
        return Optional.ofNullable(em.find(TopicJpaEntity.class, id))
                .filter(e -> e.getDeletedAt() == null && e.getOrganizationId().equals(organizationId))
                .map(topicMapper::toDomain);
    }

    @Override
    public List<Topic> findBySubjectId(String subjectId, String organizationId) {
        return em.createQuery(
                        "SELECT t FROM TopicJpaEntity t WHERE t.subjectId = :sid AND t.organizationId = :orgId AND t.deletedAt IS NULL ORDER BY t.position ASC",
                        TopicJpaEntity.class)
                .setParameter("sid", subjectId)
                .setParameter("orgId", organizationId)
                .getResultList()
                .stream()
                .map(topicMapper::toDomain)
                .toList();
    }

    @Override
    public int maxPositionBySubjectId(String subjectId, String organizationId) {
        var result = em.createQuery(
                        "SELECT COALESCE(MAX(t.position), 0) FROM TopicJpaEntity t WHERE t.subjectId = :sid AND t.organizationId = :orgId AND t.deletedAt IS NULL",
                        Integer.class)
                .setParameter("sid", subjectId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
        return result != null ? result : 0;
    }

    @Override
    @Transactional
    public void softDeleteBySubjectId(String subjectId, String organizationId) {
        em.createQuery(
                        "UPDATE TopicJpaEntity t SET t.deletedAt = :now WHERE t.subjectId = :sid AND t.organizationId = :orgId AND t.deletedAt IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("sid", subjectId)
                .setParameter("orgId", organizationId)
                .executeUpdate();
    }
}
