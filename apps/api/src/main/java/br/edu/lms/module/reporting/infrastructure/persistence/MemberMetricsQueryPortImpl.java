package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.ActivityItem;
import br.edu.lms.module.reporting.domain.model.ActivityType;
import br.edu.lms.module.reporting.domain.model.DashboardPeriod;
import br.edu.lms.module.reporting.domain.port.out.MemberMetricsQueryPort;
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
public class MemberMetricsQueryPortImpl implements MemberMetricsQueryPort {

    private static final String MEMBER_ENTITY =
            "br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity";

    private final EntityManager em;

    @Override
    public Map<String, Long> countByRole(String organizationId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT m.role, COUNT(m) FROM " + MEMBER_ENTITY + " m " +
                                "WHERE m.organizationId = :orgId AND m.deletedAt IS NULL " +
                                "GROUP BY m.role",
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
        List<Tuple> rows = em.createQuery(
                        "SELECT m.id, m.role, m.joinedAt FROM " + MEMBER_ENTITY + " m " +
                                "WHERE m.organizationId = :orgId AND m.deletedAt IS NULL " +
                                "AND m.joinedAt >= :start AND m.joinedAt < :end",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .setParameter("start", period.startInclusive())
                .setParameter("end", period.endExclusive())
                .getResultList();

        return rows.stream()
                .map(row -> new ActivityItem(
                        ActivityType.MEMBER_JOINED,
                        row.get(0, String.class),
                        "Novo membro (" + row.get(1, String.class) + ") ingressou na organização",
                        row.get(2, LocalDateTime.class)))
                .toList();
    }
}
