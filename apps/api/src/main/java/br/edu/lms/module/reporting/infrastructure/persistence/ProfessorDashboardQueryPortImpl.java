package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.curriculum.domain.port.in.SubjectDirectoryPort;
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

    private static final String CLASSROOM_MEMBER_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity";
    private static final String TASK_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskJpaEntity";
    private static final String SUBMISSION_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskSubmissionJpaEntity";
    private static final String USER_ENTITY =
            "br.edu.lms.module.identity.infrastructure.persistence.UserJpaEntity";

    private final EntityManager em;
    private final SubjectDirectoryPort subjectDirectory;

    @Override
    public boolean isProfessorAssignedToSubject(String subjectId, String professorId) {
        return subjectDirectory.isTeacherOfSubject(subjectId, professorId);
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
                                "WHERE s.taskId = :taskId AND s.deletedAt IS NULL AND s.status = 'EVALUATED' " +
                                "AND s.grade IS NOT NULL",
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

        List<String> classroomIds = subjectDirectory.findClassroomIdsBySubject(subjectId);
        if (classroomIds.isEmpty()) {
            return List.of();
        }

        List<Tuple> eligibleStudents = em.createQuery(
                        "SELECT cm.userId, u.fullName FROM " + CLASSROOM_MEMBER_ENTITY + " cm, " + USER_ENTITY + " u " +
                                "WHERE cm.userId = u.id AND cm.role = 'ALUNO' AND cm.deletedAt IS NULL " +
                                "AND cm.classroomId IN :classroomIds",
                        Tuple.class)
                .setParameter("classroomIds", classroomIds)
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
                                "AND s.grade IS NOT NULL " +
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
