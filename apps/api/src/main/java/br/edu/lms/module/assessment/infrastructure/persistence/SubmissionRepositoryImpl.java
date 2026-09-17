package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.model.SubmissionAttachment;
import br.edu.lms.module.assessment.domain.model.SubmissionCounts;
import br.edu.lms.module.assessment.domain.model.SubmissionId;
import br.edu.lms.module.assessment.domain.model.TaskSubmission;
import br.edu.lms.module.assessment.domain.port.out.SubmissionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // `createdAt` é `updatable = false`: o banco mantém o valor, mas o merge
        // copia o null da entidade destacada para a gerenciada. Sem reler o
        // estado persistido, a resposta sai com `createdAt: null` e o Zod do
        // front recusa a resposta inteira — a avaliação salva e a tela não sabe.
        em.refresh(managed);
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

    @Override
    @Transactional
    public Map<String, SubmissionCounts> countByTasks(List<String> taskIds, String organizationId) {
        // IN () vazio é erro de sintaxe em SQL; sem tarefa não há o que contar.
        if (taskIds == null || taskIds.isEmpty()) return Map.of();

        TypedQuery<Object[]> q = em.createQuery(
                """
                        SELECT s.taskId, COUNT(s), SUM(CASE WHEN s.status = 'SUBMITTED' THEN 1 ELSE 0 END)
                        FROM TaskSubmissionJpaEntity s
                        WHERE s.taskId IN :taskIds AND s.organizationId = :orgId AND s.deletedAt IS NULL
                        GROUP BY s.taskId
                        """,
                Object[].class);
        q.setParameter("taskIds", taskIds);
        q.setParameter("orgId", organizationId);

        return q.getResultList().stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> new SubmissionCounts(
                        ((Number) row[1]).longValue(),
                        row[2] == null ? 0L : ((Number) row[2]).longValue())));
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
