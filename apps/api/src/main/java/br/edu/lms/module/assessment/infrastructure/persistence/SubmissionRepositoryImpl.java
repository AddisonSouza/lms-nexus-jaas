package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.SubmissionAttachment;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.SubmissionStatus;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
public class SubmissionRepositoryImpl implements SubmissionRepository {

    private final EntityManager em;

    @Override
    @Transactional
    public TaskSubmission save(TaskSubmission submission) {
        var entity = toEntity(submission);
        var managed = em.merge(entity);
        em.flush();
        return toDomain(managed);
    }

    @Override
    @Transactional
    public Optional<TaskSubmission> findById(SubmissionId id) {
        var entity = em.find(TaskSubmissionJpaEntity.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) return Optional.empty();
        return Optional.of(toDomain(entity));
    }

    @Override
    @Transactional
    public Optional<TaskSubmission> findByTaskAndStudent(String taskId, String studentId) {
        TypedQuery<TaskSubmissionJpaEntity> q = em.createQuery(
                "SELECT s FROM TaskSubmissionJpaEntity s WHERE s.taskId = :taskId AND s.studentId = :studentId AND s.deletedAt IS NULL",
                TaskSubmissionJpaEntity.class);
        q.setParameter("taskId", taskId);
        q.setParameter("studentId", studentId);
        return q.getResultStream().findFirst().map(this::toDomain);
    }

    @Override
    @Transactional
    public List<TaskSubmission> findByTask(String taskId, String organizationId) {
        TypedQuery<TaskSubmissionJpaEntity> q = em.createQuery(
                "SELECT s FROM TaskSubmissionJpaEntity s WHERE s.taskId = :taskId AND s.organizationId = :orgId AND s.deletedAt IS NULL",
                TaskSubmissionJpaEntity.class);
        q.setParameter("taskId", taskId);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(this::toDomain).toList();
    }

    private TaskSubmissionJpaEntity toEntity(TaskSubmission submission) {
        var entity = new TaskSubmissionJpaEntity();
        entity.setId(submission.getId().getValue());
        entity.setTaskId(submission.getTaskId());
        entity.setStudentId(submission.getStudentId());
        entity.setOrganizationId(submission.getOrganizationId());
        entity.setTextResponse(submission.getTextResponse());
        entity.setStatus(submission.getStatus().name());
        entity.setGrade(submission.getGrade());
        entity.setFeedback(submission.getFeedback());
        entity.setCreatedAt(submission.getCreatedAt());
        entity.setUpdatedAt(submission.getUpdatedAt());
        entity.setDeletedAt(submission.getDeletedAt());

        if (submission.getAttachments() != null) {
            List<SubmissionAttachmentJpaEntity> attachmentEntities = submission.getAttachments().stream()
                    .map(a -> {
                        var ae = new SubmissionAttachmentJpaEntity();
                        ae.setId(a.id());
                        ae.setSubmission(entity);
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

    private TaskSubmission toDomain(TaskSubmissionJpaEntity e) {
        List<SubmissionAttachment> attachments = e.getAttachments() == null ? List.of() :
                e.getAttachments().stream()
                        .map(a -> new SubmissionAttachment(a.getId(), a.getFileKey(), a.getOriginalName(), a.getMimeType(), a.getSizeBytes()))
                        .toList();

        return TaskSubmission.builder()
                .id(SubmissionId.of(e.getId()))
                .taskId(e.getTaskId())
                .studentId(e.getStudentId())
                .organizationId(e.getOrganizationId())
                .textResponse(e.getTextResponse())
                .status(SubmissionStatus.valueOf(e.getStatus()))
                .grade(e.getGrade())
                .feedback(e.getFeedback())
                .attachments(attachments)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
