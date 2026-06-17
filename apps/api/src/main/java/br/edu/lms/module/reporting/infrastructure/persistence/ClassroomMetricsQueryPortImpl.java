package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.ActivityType;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.out.ClassroomMetricsQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class ClassroomMetricsQueryPortImpl implements ClassroomMetricsQueryPort {

    private static final String CLASSROOM_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomJpaEntity";

    private final EntityManager em;

    @Override
    public Map<String, Long> countByStatus(String organizationId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT c.status, COUNT(c) FROM " + CLASSROOM_ENTITY + " c " +
                                "WHERE c.organizationId = :orgId AND c.deletedAt IS NULL " +
                                "GROUP BY c.status",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .getResultList();

        Map<String, Long> result = new HashMap<>();
        for (Tuple row : rows) {
            result.put(row.get(0, String.class), row.get(1, Long.class));
        }
        return result;
    }

    @Override
    public List<ActivityItem> listActivity(String organizationId, DashboardPeriod period) {
        LocalDateTime start = period.startInclusive();
        LocalDateTime end = period.endExclusive();

        List<Tuple> rows = em.createQuery(
                        "SELECT c.id, c.name, c.status, c.createdAt, c.updatedAt FROM " + CLASSROOM_ENTITY + " c " +
                                "WHERE c.organizationId = :orgId AND c.deletedAt IS NULL " +
                                "AND ((c.createdAt >= :start AND c.createdAt < :end) " +
                                "  OR (c.status = 'ARCHIVED' AND c.updatedAt >= :start AND c.updatedAt < :end))",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        return rows.stream().map(row -> {
            String id = row.get(0, String.class);
            String name = row.get(1, String.class);
            String status = row.get(2, String.class);
            LocalDateTime createdAt = row.get(3, LocalDateTime.class);
            LocalDateTime updatedAt = row.get(4, LocalDateTime.class);

            boolean isArchiveEvent = "ARCHIVED".equals(status)
                    && updatedAt != null
                    && !updatedAt.isBefore(start)
                    && updatedAt.isBefore(end);

            if (isArchiveEvent) {
                return new ActivityItem(ActivityType.CLASSROOM_ARCHIVED, id,
                        "Turma \"" + name + "\" arquivada", updatedAt);
            }
            return new ActivityItem(ActivityType.CLASSROOM_CREATED, id,
                    "Turma \"" + name + "\" criada", createdAt);
        }).toList();
    }
}
