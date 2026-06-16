package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.SubmissionAttachment;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
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
    private final SubmissionMapper submissionMapper;

    @Override
    @Transactional
    public TaskSubmission save(TaskSubmission submission) {
        var entity = toEntityWithAttachments(submission);
        var managed = em.merge(entity);
        em.flush();
        return submissionMapper.toDomain(managed);
    }

    @Override
    @Transactional
    public Optional<TaskSubmission> findById(SubmissionId id) {
        var entity = em.find(TaskSubmissionJpaEntity.class, id.getValue());
        if (entity == null || entity.getDeletedAt() != null) return Optional.empty();
        return Optional.of(submissionMapper.toDomain(entity));
    }

    @Override
    @Transactional
    public Optional<TaskSubmission> findByTaskAndStudent(String taskId, String studentId) {
        TypedQuery<TaskSubmissionJpaEntity> q = em.createQuery(
                "SELECT s FROM TaskSubmissionJpaEntity s WHERE s.taskId = :taskId AND s.studentId = :studentId AND s.deletedAt IS NULL",
                TaskSubmissionJpaEntity.class);
        q.setParameter("taskId", taskId);
        q.setParameter("studentId", studentId);
        return q.getResultStream().findFirst().map(submissionMapper::toDomain);
    }

    @Override
    @Transactional
    public List<TaskSubmission> findByTask(String taskId, String organizationId) {
        TypedQuery<TaskSubmissionJpaEntity> q = em.createQuery(
                "SELECT s FROM TaskSubmissionJpaEntity s WHERE s.taskId = :taskId AND s.organizationId = :orgId AND s.deletedAt IS NULL",
                TaskSubmissionJpaEntity.class);
        q.setParameter("taskId", taskId);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(submissionMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public List<TaskSubmission> findByStudentAndOrganization(String studentId, String organizationId) {
        TypedQuery<TaskSubmissionJpaEntity> q = em.createQuery(
                "SELECT s FROM TaskSubmissionJpaEntity s WHERE s.studentId = :studentId AND s.organizationId = :orgId AND s.deletedAt IS NULL",
                TaskSubmissionJpaEntity.class);
        q.setParameter("studentId", studentId);
        q.setParameter("orgId", organizationId);
        return q.getResultList().stream().map(submissionMapper::toDomain).toList();
    }

    /**
     * Uses SubmissionMapper for main fields but handles attachments manually
     * because of the bi-directional JPA back-reference (ae.setSubmission(entity)).
     */
    private TaskSubmissionJpaEntity toEntityWithAttachments(TaskSubmission submission) {
        var entity = submissionMapper.toEntity(submission);

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
}
