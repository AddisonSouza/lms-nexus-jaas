package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.ActivityType;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.out.TaskMetricsQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class TaskMetricsQueryPortImpl implements TaskMetricsQueryPort {

    private static final String TASK_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskJpaEntity";
    private static final String SUBMISSION_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskSubmissionJpaEntity";
    private static final String SUBJECT_CLASSROOM_ENTITY =
            "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectClassroomJpaEntity";
    private static final String CLASSROOM_MEMBER_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity";

    private final EntityManager em;

    @Override
    public long countCreated(String organizationId, DashboardPeriod period) {
        return em.createQuery(
                        "SELECT COUNT(t) FROM " + TASK_ENTITY + " t " +
                                "WHERE t.organizationId = :orgId AND t.deletedAt IS NULL " +
                                "AND t.createdAt >= :start AND t.createdAt < :end",
                        Long.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", period.startInclusive())
                .setParameter("end", period.endExclusive())
                .getSingleResult();
    }

    @Override
    public long countEvaluated(String organizationId, DashboardPeriod period) {
        return em.createQuery(
                        "SELECT COUNT(s) FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.organizationId = :orgId AND s.deletedAt IS NULL AND s.status = 'EVALUATED' " +
                                "AND s.updatedAt >= :start AND s.updatedAt < :end",
                        Long.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", period.startInclusive())
                .setParameter("end", period.endExclusive())
                .getSingleResult();
    }

    @Override
    public BigDecimal averageDeliveryRate(String organizationId, DashboardPeriod period) {
        List<Tuple> tasks = em.createQuery(
                        "SELECT t.id, t.subjectId FROM " + TASK_ENTITY + " t " +
                                "WHERE t.organizationId = :orgId AND t.deletedAt IS NULL " +
                                "AND t.createdAt >= :start AND t.createdAt < :end",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", period.startInclusive())
                .setParameter("end", period.endExclusive())
                .getResultList();

        List<BigDecimal> rates = new ArrayList<>();
        for (Tuple task : tasks) {
            String taskId = task.get(0, String.class);
            String subjectId = task.get(1, String.class);

            long eligible = countEligibleStudents(subjectId);
            if (eligible == 0) {
                continue;
            }
            long submitted = countSubmitted(taskId);
            rates.add(BigDecimal.valueOf(submitted)
                    .divide(BigDecimal.valueOf(eligible), 4, RoundingMode.HALF_UP));
        }

        if (rates.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(rates.size()), 4, RoundingMode.HALF_UP);
    }

    private long countEligibleStudents(String subjectId) {
        return em.createQuery(
                        "SELECT COUNT(cm) FROM " + CLASSROOM_MEMBER_ENTITY + " cm " +
                                "WHERE cm.role = 'ALUNO' AND cm.deletedAt IS NULL " +
                                "AND cm.classroomId IN (" +
                                "  SELECT sc.id.classroomId FROM " + SUBJECT_CLASSROOM_ENTITY + " sc " +
                                "  WHERE sc.id.subjectId = :subjectId" +
                                ")",
                        Long.class)
                .setParameter("subjectId", subjectId)
                .getSingleResult();
    }

    private long countSubmitted(String taskId) {
        return em.createQuery(
                        "SELECT COUNT(s) FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.taskId = :taskId AND s.deletedAt IS NULL " +
                                "AND s.status IN ('SUBMITTED', 'EVALUATED')",
                        Long.class)
                .setParameter("taskId", taskId)
                .getSingleResult();
    }

    @Override
    public List<ActivityItem> listActivity(String organizationId, DashboardPeriod period) {
        LocalDateTime start = period.startInclusive();
        LocalDateTime end = period.endExclusive();
        List<ActivityItem> activity = new ArrayList<>();

        List<Tuple> createdTasks = em.createQuery(
                        "SELECT t.id, t.title, t.createdAt FROM " + TASK_ENTITY + " t " +
                                "WHERE t.organizationId = :orgId AND t.deletedAt IS NULL " +
                                "AND t.createdAt >= :start AND t.createdAt < :end",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        for (Tuple row : createdTasks) {
            activity.add(new ActivityItem(ActivityType.TASK_CREATED, row.get(0, String.class),
                    "Tarefa \"" + row.get(1, String.class) + "\" criada", row.get(2, LocalDateTime.class)));
        }

        List<Tuple> evaluatedSubmissions = em.createQuery(
                        "SELECT s.id, s.taskId, s.updatedAt FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.organizationId = :orgId AND s.deletedAt IS NULL AND s.status = 'EVALUATED' " +
                                "AND s.updatedAt >= :start AND s.updatedAt < :end",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        for (Tuple row : evaluatedSubmissions) {
            activity.add(new ActivityItem(ActivityType.TASK_EVALUATED, row.get(1, String.class),
                    "Submissão avaliada", row.get(2, LocalDateTime.class)));
        }

        return activity;
    }
}
