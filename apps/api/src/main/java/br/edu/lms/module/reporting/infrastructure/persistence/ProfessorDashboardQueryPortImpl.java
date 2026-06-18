package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.StudentAverageGrade;
import br.edu.lms.module.reporting.domain.model.StudentSummary;
import br.edu.lms.module.reporting.domain.port.out.ProfessorDashboardQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
public class ProfessorDashboardQueryPortImpl implements ProfessorDashboardQueryPort {

    private static final String SUBJECT_TEACHER_ENTITY =
            "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectTeacherJpaEntity";
    private static final String ORGANIZATION_MEMBER_ENTITY =
            "br.edu.lms.module.organization.infrastructure.persistence.OrganizationMemberJpaEntity";
    private static final String SUBJECT_CLASSROOM_ENTITY =
            "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectClassroomJpaEntity";
    private static final String CLASSROOM_MEMBER_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity";
    private static final String TASK_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskJpaEntity";
    private static final String SUBMISSION_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskSubmissionJpaEntity";
    private static final String USER_ENTITY =
            "br.edu.lms.module.identity.infrastructure.persistence.UserJpaEntity";

    private final EntityManager em;

    @Override
    public boolean isProfessorAssignedToSubject(String subjectId, String professorId) {
        long count = em.createQuery(
                        "SELECT COUNT(st) FROM " + SUBJECT_TEACHER_ENTITY + " st, " + ORGANIZATION_MEMBER_ENTITY + " m " +
                                "WHERE st.id.subjectId = :subjectId AND st.id.memberId = m.id " +
                                "AND m.userId = :userId AND m.deletedAt IS NULL",
                        Long.class)
                .setParameter("subjectId", subjectId)
                .setParameter("userId", professorId)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public long countPendingEvaluations(String subjectId) {
        return em.createQuery(
                        "SELECT COUNT(s) FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.deletedAt IS NULL AND s.status = 'SUBMITTED' AND s.taskId IN (" +
                                "  SELECT t.id FROM " + TASK_ENTITY + " t " +
                                "  WHERE t.deletedAt IS NULL AND t.subjectId = :subjectId" +
                                ")",
                        Long.class)
                .setParameter("subjectId", subjectId)
                .getSingleResult();
    }

    @Override
    public List<BigDecimal> getLastTaskGradeDistribution(String subjectId) {
        String lastTaskId = findLastTaskId(subjectId);
        if (lastTaskId == null) {
            return List.of();
        }

        return em.createQuery(
                        "SELECT s.grade FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.taskId = :taskId AND s.deletedAt IS NULL AND s.status = 'EVALUATED'",
                        BigDecimal.class)
                .setParameter("taskId", lastTaskId)
                .getResultList();
    }

    @Override
    public List<StudentSummary> getLastTaskStudentsWithoutSubmission(String subjectId) {
        String lastTaskId = findLastTaskId(subjectId);
        if (lastTaskId == null) {
            return List.of();
        }

        List<Tuple> eligibleStudents = em.createQuery(
                        "SELECT cm.userId, u.fullName FROM " + CLASSROOM_MEMBER_ENTITY + " cm, " + USER_ENTITY + " u " +
                                "WHERE cm.userId = u.id AND cm.role = 'ALUNO' AND cm.deletedAt IS NULL " +
                                "AND cm.classroomId IN (" +
                                "  SELECT sc.id.classroomId FROM " + SUBJECT_CLASSROOM_ENTITY + " sc " +
                                "  WHERE sc.id.subjectId = :subjectId" +
                                ")",
                        Tuple.class)
                .setParameter("subjectId", subjectId)
                .getResultList();

        if (eligibleStudents.isEmpty()) {
            return List.of();
        }

        Set<String> submittedStudentIds = new HashSet<>(em.createQuery(
                        "SELECT s.studentId FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.taskId = :taskId AND s.deletedAt IS NULL",
                        String.class)
                .setParameter("taskId", lastTaskId)
                .getResultList());

        return eligibleStudents.stream()
                .filter(row -> !submittedStudentIds.contains(row.get(0, String.class)))
                .map(row -> new StudentSummary(row.get(0, String.class), row.get(1, String.class)))
                .toList();
    }

    @Override
    public List<StudentAverageGrade> getAverageGradePerStudent(String subjectId) {
        List<Tuple> rows = em.createQuery(
                        "SELECT s.studentId, u.fullName, AVG(s.grade) FROM " + SUBMISSION_ENTITY + " s, " + USER_ENTITY + " u " +
                                "WHERE s.studentId = u.id AND s.deletedAt IS NULL AND s.status = 'EVALUATED' " +
                                "AND s.taskId IN (" +
                                "  SELECT t.id FROM " + TASK_ENTITY + " t " +
                                "  WHERE t.deletedAt IS NULL AND t.subjectId = :subjectId" +
                                ") " +
                                "GROUP BY s.studentId, u.fullName",
                        Tuple.class)
                .setParameter("subjectId", subjectId)
                .getResultList();

        return rows.stream()
                .map(row -> new StudentAverageGrade(
                        row.get(0, String.class),
                        row.get(1, String.class),
                        BigDecimal.valueOf(row.get(2, Double.class)).setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private String findLastTaskId(String subjectId) {
        List<String> taskIds = em.createQuery(
                        "SELECT t.id FROM " + TASK_ENTITY + " t " +
                                "WHERE t.deletedAt IS NULL AND t.subjectId = :subjectId " +
                                "ORDER BY t.createdAt DESC",
                        String.class)
                .setParameter("subjectId", subjectId)
                .setMaxResults(1)
                .getResultList();
        return taskIds.isEmpty() ? null : taskIds.get(0);
    }
}
