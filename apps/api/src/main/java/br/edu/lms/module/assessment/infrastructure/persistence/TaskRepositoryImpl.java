package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskAttachment;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.port.out.TaskRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepository {

    private final EntityManager em;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public Task save(Task task) {
        var entity = toEntityWithAttachments(task);
        var managed = em.merge(entity);
        em.flush();
        return taskMapper.toDomain(managed);
    }

    @Override
    @Transactional
    public Optional<Task> findById(TaskId id) {
        var entity = em.find(TaskJpaEntity.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) return Optional.empty();
        return Optional.of(taskMapper.toDomain(entity));
    }

    @Override
    @Transactional
    public Optional<Task> findByIdAndOrganization(TaskId id, String organizationId) {
        return findById(id)
                .filter(t -> t.getOrganizationId().equals(organizationId));
    }

    @Override
    @Transactional
    public List<Task> findByOrganizationAndCreatedBy(String organizationId, String createdBy) {
        TypedQuery<TaskJpaEntity> q = em.createQuery(
                "SELECT t FROM TaskJpaEntity t WHERE t.organizationId = :orgId AND t.createdBy = :uid AND t.deletedAt IS NULL ORDER BY t.createdAt DESC",
                TaskJpaEntity.class);
        q.setParameter("orgId", organizationId);
        q.setParameter("uid", createdBy);
        return q.getResultList().stream().map(taskMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public List<Task> findPublishedByOrganization(String organizationId) {
        TypedQuery<TaskJpaEntity> q = em.createQuery(
                "SELECT t FROM TaskJpaEntity t WHERE t.organizationId = :orgId AND t.status = 'PUBLISHED' AND t.deletedAt IS NULL ORDER BY t.deadline ASC",
                TaskJpaEntity.class);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(taskMapper::toDomain).toList();
    }

    /**
     * Uses TaskMapper for main fields but handles attachments manually
     * because of the bi-directional JPA back-reference (ae.setTask(entity)).
     */
    private TaskJpaEntity toEntityWithAttachments(Task task) {
        var entity = taskMapper.toEntity(task);

        if (task.getAttachments() != null) {
            List<TaskAttachmentJpaEntity> attachmentEntities = task.getAttachments().stream()
                    .map(a -> {
                        var ae = new TaskAttachmentJpaEntity();
                        ae.setId(a.id());
                        ae.setTask(entity);
                        ae.setFileKey(a.fileKey());
                        ae.setOriginalName(a.originalName());
                        ae.setMimeType(a.mimeType());
                        ae.setSizeBytes(a.sizeBytes());
                        return ae;
                    }).toList();
            entity.setAttachments(attachmentEntities);
        }
        return entity;
    }
}
