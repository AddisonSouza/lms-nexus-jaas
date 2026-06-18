package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.RecentGrade;
import br.edu.lms.module.reporting.domain.model.SubjectAverageGrade;
import br.edu.lms.module.reporting.domain.model.UpcomingTask;
import br.edu.lms.module.reporting.domain.port.out.StudentDashboardQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class StudentDashboardQueryPortImpl implements StudentDashboardQueryPort {

    private static final String TASK_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskJpaEntity";
    private static final String SUBMISSION_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskSubmissionJpaEntity";
    private static final String SUBJECT_ENTITY =
            "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectJpaEntity";
    private static final String SUBJECT_CLASSROOM_ENTITY =
            "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectClassroomJpaEntity";
    private static final String CLASSROOM_MEMBER_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity";

    private final EntityManager em;

    @Override
    public List<UpcomingTask> getUpcomingPendingTasks(String studentId, String organizationId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT t.id, t.title, sub.name, t.deadline FROM " + TASK_ENTITY + " t, " + SUBJECT_ENTITY + " sub " +
                                "WHERE t.subjectId = sub.id AND t.deletedAt IS NULL AND t.status = 'PUBLISHED' " +
                                "AND t.organizationId = :organizationId " +
                                "AND t.subjectId IN (" + eligibleSubjectIdsSubquery() + ") " +
                                "AND t.id NOT IN (" + studentSubmittedTaskIdsSubquery() + ") " +
                                "ORDER BY t.deadline ASC",
                        Tuple.class)
                .setParameter("studentId", studentId)
                .setParameter("organizationId", organizationId)
                .getResultList();

        return rows.stream()
                .map(row -> new UpcomingTask(
                        row.get(0, String.class),
                        row.get(1, String.class),
                        row.get(2, String.class),
                        row.get(3, java.time.LocalDateTime.class)))
                .toList();
    }

    @Override
    public long countPendingTasks(String studentId, String organizationId) {
        return em.createQuery(
                        "SELECT COUNT(t) FROM " + TASK_ENTITY + " t " +
                                "WHERE t.deletedAt IS NULL AND t.status = 'PUBLISHED' AND t.organizationId = :organizationId " +
                                "AND t.subjectId IN (" + eligibleSubjectIdsSubquery() + ") " +
                                "AND t.id NOT IN (" + studentSubmittedTaskIdsSubquery() + ")",
                        Long.class)
                .setParameter("studentId", studentId)
                .setParameter("organizationId", organizationId)
                .getSingleResult();
    }

    @Override
    public long countSubmittedTasks(String studentId, String organizationId) {
        return em.createQuery(
                        "SELECT COUNT(t) FROM " + TASK_ENTITY + " t " +
                                "WHERE t.deletedAt IS NULL AND t.status = 'PUBLISHED' AND t.organizationId = :organizationId " +
                                "AND t.subjectId IN (" + eligibleSubjectIdsSubquery() + ") " +
                                "AND t.id IN (" + studentSubmittedTaskIdsSubquery() + ")",
                        Long.class)
                .setParameter("studentId", studentId)
                .setParameter("organizationId", organizationId)
                .getSingleResult();
    }

    @Override
    public List<RecentGrade> getRecentGrades(String studentId, String organizationId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT s.taskId, t.title, sub.name, s.grade, s.feedback " +
                                "FROM " + SUBMISSION_ENTITY + " s, " + TASK_ENTITY + " t, " + SUBJECT_ENTITY + " sub " +
                                "WHERE s.taskId = t.id AND t.subjectId = sub.id " +
                                "AND s.studentId = :studentId AND s.organizationId = :organizationId " +
                                "AND s.deletedAt IS NULL AND s.status = 'EVALUATED' " +
                                "ORDER BY s.updatedAt DESC",
                        Tuple.class)
                .setParameter("studentId", studentId)
                .setParameter("organizationId", organizationId)
                .setMaxResults(5)
                .getResultList();

        return rows.stream()
                .map(row -> new RecentGrade(
                        row.get(0, String.class),
                        row.get(1, String.class),
                        row.get(2, String.class),
                        row.get(3, BigDecimal.class),
                        row.get(4, String.class)))
                .toList();
    }

    @Override
    public List<SubjectAverageGrade> getAverageGradePerSubject(String studentId, String organizationId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT t.subjectId, sub.name, AVG(s.grade) " +
                                "FROM " + SUBMISSION_ENTITY + " s, " + TASK_ENTITY + " t, " + SUBJECT_ENTITY + " sub " +
                                "WHERE s.taskId = t.id AND t.subjectId = sub.id " +
                                "AND s.studentId = :studentId AND s.organizationId = :organizationId " +
                                "AND s.deletedAt IS NULL AND s.status = 'EVALUATED' " +
                                "GROUP BY t.subjectId, sub.name",
                        Tuple.class)
                .setParameter("studentId", studentId)
                .setParameter("organizationId", organizationId)
                .getResultList();

        return rows.stream()
                .map(row -> new SubjectAverageGrade(
                        row.get(0, String.class),
                        row.get(1, String.class),
                        BigDecimal.valueOf(row.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private String eligibleSubjectIdsSubquery() {
        return "SELECT sc.id.subjectId FROM " + SUBJECT_CLASSROOM_ENTITY + " sc " +
                "WHERE sc.id.classroomId IN (" +
                "  SELECT cm.classroomId FROM " + CLASSROOM_MEMBER_ENTITY + " cm " +
                "  WHERE cm.userId = :studentId AND cm.role = 'ALUNO' AND cm.deletedAt IS NULL " +
                "  AND cm.organizationId = :organizationId" +
                ")";
    }

    private String studentSubmittedTaskIdsSubquery() {
        return "SELECT s.taskId FROM " + SUBMISSION_ENTITY + " s " +
                "WHERE s.studentId = :studentId AND s.deletedAt IS NULL";
    }
}
