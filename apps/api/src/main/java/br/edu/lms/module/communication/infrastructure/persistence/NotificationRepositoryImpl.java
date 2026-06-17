package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.exception.NotificationNotFoundException;
import br.edu.lms.module.communication.domain.model.Notification;
import br.edu.lms.module.communication.domain.model.NotificationId;
import br.edu.lms.module.communication.domain.port.out.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final EntityManager em;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        var entity = notificationMapper.toEntity(notification);
        var managed = em.merge(entity);
        em.flush();
        return notificationMapper.toDomain(managed);
    }

    @Override
    @Transactional
    public Optional<Notification> findById(NotificationId id, String organizationId) {
        var entity = em.find(NotificationJpaEntity.class, id.getValue());
        if (entity == null || !entity.getOrganizationId().equals(organizationId)) {
            return Optional.empty();
        }
        return Optional.of(notificationMapper.toDomain(entity));
    }

    @Override
    @Transactional
    public List<Notification> findByUser(String userId, String organizationId) {
        TypedQuery<NotificationJpaEntity> q = em.createQuery(
                "SELECT n FROM NotificationJpaEntity n WHERE n.userId = :userId " +
                        "AND n.organizationId = :orgId ORDER BY n.createdAt DESC",
                NotificationJpaEntity.class);
        q.setParameter("userId", userId);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(notificationMapper::toDomain).toList();
    }

    @Override
    public long countUnreadByUser(String userId, String organizationId) {
        return em.createQuery(
                        "SELECT COUNT(n) FROM NotificationJpaEntity n WHERE n.userId = :userId " +
                                "AND n.organizationId = :orgId AND n.readAt IS NULL",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("orgId", organizationId)
                .getSingleResult();
    }

    @Override
    @Transactional
    public Notification markRead(NotificationId id) {
        var entity = em.find(NotificationJpaEntity.class, id.getValue());
        if (entity == null) {
            throw new NotificationNotFoundException(id.getValue());
        }
        if (entity.getReadAt() == null) {
            entity.setReadAt(LocalDateTime.now());
            entity = em.merge(entity);
            em.flush();
        }
        return notificationMapper.toDomain(entity);
    }

    @Override
    @Transactional
    public void markAllReadByUser(String userId, String organizationId) {
        em.createQuery(
                        "UPDATE NotificationJpaEntity n SET n.readAt = :now WHERE n.userId = :userId " +
                                "AND n.organizationId = :orgId AND n.readAt IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("userId", userId)
                .setParameter("orgId", organizationId)
                .executeUpdate();
    }
}
