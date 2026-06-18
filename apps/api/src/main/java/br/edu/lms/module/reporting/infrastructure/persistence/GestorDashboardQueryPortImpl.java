package br.edu.lms.module.reporting.infrastructure.persistence;

import br.edu.lms.module.reporting.domain.model.AtRiskStudent;
import br.edu.lms.module.reporting.domain.model.ClassroomHealth;
import br.edu.lms.module.reporting.domain.port.out.GestorDashboardQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class GestorDashboardQueryPortImpl implements GestorDashboardQueryPort {

    private static final String CLASSROOM_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomJpaEntity";
    private static final String CLASSROOM_MEMBER_ENTITY =
            "br.edu.lms.module.classroom.infrastructure.persistence.ClassroomMemberJpaEntity";
    private static final String SUBJECT_CLASSROOM_ENTITY =
            "br.edu.lms.module.curriculum.infrastructure.persistence.SubjectClassroomJpaEntity";
    private static final String TASK_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskJpaEntity";
    private static final String SUBMISSION_ENTITY =
            "br.edu.lms.module.assessment.infrastructure.persistence.TaskSubmissionJpaEntity";
    private static final String USER_ENTITY =
            "br.edu.lms.module.identity.infrastructure.persistence.UserJpaEntity";

    private final EntityManager em;

    @Override
    public List<ClassroomHealth> getClassroomsHealth(String organizationId) {
        List<Tuple> classrooms = em.createQuery(
                        "SELECT c.id, c.name, c.status FROM " + CLASSROOM_ENTITY + " c " +
                                "WHERE c.organizationId = :orgId AND c.deletedAt IS NULL",
                        Tuple.class)
                .setParameter("orgId", organizationId)
                .getResultList();

        List<ClassroomHealth> result = new ArrayList<>();
        for (Tuple row : classrooms) {
            String classroomId = row.get(0, String.class);
            String name = row.get(1, String.class);
            String status = row.get(2, String.class);

            List<String> taskIds = taskIdsForClassroom(classroomId);
            result.add(new ClassroomHealth(
                    classroomId,
                    name,
                    status,
                    deliveryRateForClassroom(classroomId, taskIds),
                    averageGradeForClassroom(taskIds)));
        }
        return result;
    }

    private List<String> taskIdsForClassroom(String classroomId) {
        return em.createQuery(
                        "SELECT t.id FROM " + TASK_ENTITY + " t " +
                                "WHERE t.deletedAt IS NULL AND t.subjectId IN (" +
                                "  SELECT sc.id.subjectId FROM " + SUBJECT_CLASSROOM_ENTITY + " sc " +
                                "  WHERE sc.id.classroomId = :classroomId" +
                                ")",
                        String.class)
                .setParameter("classroomId", classroomId)
                .getResultList();
    }

    private BigDecimal deliveryRateForClassroom(String classroomId, List<String> taskIds) {
        long eligible = em.createQuery(
                        "SELECT COUNT(cm) FROM " + CLASSROOM_MEMBER_ENTITY + " cm " +
                                "WHERE cm.classroomId = :classroomId AND cm.role = 'ALUNO' AND cm.deletedAt IS NULL",
                        Long.class)
                .setParameter("classroomId", classroomId)
                .getSingleResult();

        if (eligible == 0 || taskIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> rates = new ArrayList<>();
        for (String taskId : taskIds) {
            long submitted = em.createQuery(
                            "SELECT COUNT(s) FROM " + SUBMISSION_ENTITY + " s " +
                                    "WHERE s.taskId = :taskId AND s.deletedAt IS NULL " +
                                    "AND s.status IN ('SUBMITTED', 'EVALUATED')",
                            Long.class)
                    .setParameter("taskId", taskId)
                    .getSingleResult();
            rates.add(BigDecimal.valueOf(submitted)
                    .divide(BigDecimal.valueOf(eligible), 4, RoundingMode.HALF_UP));
        }

        BigDecimal sum = rates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(rates.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal averageGradeForClassroom(List<String> taskIds) {
        if (taskIds.isEmpty()) {
            return null;
        }

        List<BigDecimal> grades = em.createQuery(
                        "SELECT s.grade FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.taskId IN :taskIds AND s.deletedAt IS NULL AND s.status = 'EVALUATED'",
                        BigDecimal.class)
                .setParameter("taskIds", taskIds)
                .getResultList();

        if (grades.isEmpty()) {
            return null;
        }
        BigDecimal sum = grades.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    public List<AtRiskStudent> listAtRiskStudents(String classroomId, int limit) {
        List<Tuple> overdueTasks = em.createQuery(
                        "SELECT t.id, t.deadline FROM " + TASK_ENTITY + " t " +
                                "WHERE t.deletedAt IS NULL AND t.deadline < :now AND t.subjectId IN (" +
                                "  SELECT sc.id.subjectId FROM " + SUBJECT_CLASSROOM_ENTITY + " sc " +
                                "  WHERE sc.id.classroomId = :classroomId" +
                                ")",
                        Tuple.class)
                .setParameter("classroomId", classroomId)
                .setParameter("now", LocalDateTime.now())
                .getResultList();

        if (overdueTasks.isEmpty()) {
            return List.of();
        }

        List<String> studentIds = em.createQuery(
                        "SELECT cm.userId FROM " + CLASSROOM_MEMBER_ENTITY + " cm " +
                                "WHERE cm.classroomId = :classroomId AND cm.role = 'ALUNO' AND cm.deletedAt IS NULL",
                        String.class)
                .setParameter("classroomId", classroomId)
                .getResultList();

        if (studentIds.isEmpty()) {
            return List.of();
        }

        Map<String, LocalDateTime> deadlineByTaskId = new HashMap<>();
        for (Tuple row : overdueTasks) {
            deadlineByTaskId.put(row.get(0, String.class), row.get(1, LocalDateTime.class));
        }

        List<Tuple> submissions = em.createQuery(
                        "SELECT s.studentId, s.taskId, s.createdAt FROM " + SUBMISSION_ENTITY + " s " +
                                "WHERE s.taskId IN :taskIds AND s.studentId IN :studentIds AND s.deletedAt IS NULL",
                        Tuple.class)
                .setParameter("taskIds", deadlineByTaskId.keySet())
                .setParameter("studentIds", studentIds)
                .getResultList();

        Map<String, Map<String, LocalDateTime>> submittedAtByStudentAndTask = new HashMap<>();
        for (Tuple row : submissions) {
            submittedAtByStudentAndTask
                    .computeIfAbsent(row.get(0, String.class), k -> new HashMap<>())
                    .put(row.get(1, String.class), row.get(2, LocalDateTime.class));
        }

        Map<String, Long> pendingCountByStudent = new HashMap<>();
        for (String studentId : studentIds) {
            Map<String, LocalDateTime> submittedTasks = submittedAtByStudentAndTask.getOrDefault(studentId, Map.of());
            long pending = 0;
            for (Map.Entry<String, LocalDateTime> entry : deadlineByTaskId.entrySet()) {
                LocalDateTime submittedAt = submittedTasks.get(entry.getKey());
                if (submittedAt == null || submittedAt.isAfter(entry.getValue())) {
                    pending++;
                }
            }
            if (pending > 0) {
                pendingCountByStudent.put(studentId, pending);
            }
        }

        if (pendingCountByStudent.isEmpty()) {
            return List.of();
        }

        List<Map.Entry<String, Long>> topStudents = pendingCountByStudent.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .toList();

        Map<String, String> nameByStudentId = new HashMap<>();
        List<Tuple> names = em.createQuery(
                        "SELECT u.id, u.fullName FROM " + USER_ENTITY + " u " +
                                "WHERE u.id IN :ids",
                        Tuple.class)
                .setParameter("ids", topStudents.stream().map(Map.Entry::getKey).toList())
                .getResultList();
        for (Tuple row : names) {
            nameByStudentId.put(row.get(0, String.class), row.get(1, String.class));
        }

        return topStudents.stream()
                .map(entry -> new AtRiskStudent(
                        entry.getKey(),
                        nameByStudentId.get(entry.getKey()),
                        entry.getValue()))
                .toList();
    }
}
