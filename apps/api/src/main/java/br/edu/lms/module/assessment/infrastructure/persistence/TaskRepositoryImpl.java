package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.Task;
import br.edu.lms.module.assessment.domain.model.TaskAttachment;
import br.edu.lms.module.assessment.domain.model.TaskId;
import br.edu.lms.module.assessment.domain.model.TaskStatus;
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

    @Override
    @Transactional
    public Task save(Task task) {
        var entity = toEntity(task);
        var managed = em.merge(entity);
        em.flush();
        return toDomain(managed);
    }

    @Override
    @Transactional
    public Optional<Task> findById(TaskId id) {
        var entity = em.find(TaskJpaEntity.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) return Optional.empty();
        return Optional.of(toDomain(entity));
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
        return q.getResultList().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public List<Task> findPublishedByOrganization(String organizationId) {
        TypedQuery<TaskJpaEntity> q = em.createQuery(
                "SELECT t FROM TaskJpaEntity t WHERE t.organizationId = :orgId AND t.status = 'PUBLISHED' AND t.deletedAt IS NULL ORDER BY t.deadline ASC",
                TaskJpaEntity.class);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(this::toDomain).toList();
    }

    private TaskJpaEntity toEntity(Task task) {
        var entity = new TaskJpaEntity();
        entity.setId(task.getId().getValue());
        entity.setSubjectId(task.getSubjectId());
        entity.setOrganizationId(task.getOrganizationId());
        entity.setCreatedBy(task.getCreatedBy());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setDeadline(task.getDeadline());
        entity.setMaxScore(task.getMaxScore());
        entity.setStatus(task.getStatus().name());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        entity.setDeletedAt(task.getDeletedAt());

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

    private Task toDomain(TaskJpaEntity e) {
        List<TaskAttachment> attachments = e.getAttachments() == null ? List.of() :
                e.getAttachments().stream()
                        .map(a -> new TaskAttachment(a.getId(), a.getFileKey(), a.getOriginalName(), a.getMimeType(), a.getSizeBytes()))
                        .toList();

        return Task.builder()
                .id(TaskId.of(e.getId()))
                .subjectId(e.getSubjectId())
                .organizationId(e.getOrganizationId())
                .createdBy(e.getCreatedBy())
                .title(e.getTitle())
                .description(e.getDescription())
                .deadline(e.getDeadline())
                .maxScore(e.getMaxScore())
                .status(TaskStatus.valueOf(e.getStatus()))
                .attachments(attachments)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
