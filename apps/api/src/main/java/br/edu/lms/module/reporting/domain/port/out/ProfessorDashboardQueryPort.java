package br.edu.lms.module.reporting.domain.port.out;

import br.edu.lms.module.reporting.domain.model.StudentAverageGrade;
import br.edu.lms.module.reporting.domain.model.StudentSummary;

import java.math.BigDecimal;
import java.util.List;

public interface ProfessorDashboardQueryPort {
    boolean isProfessorAssignedToSubject(String subjectId, String professorId);
    long countPendingEvaluations(String subjectId);
    List<BigDecimal> getLastTaskGradeDistribution(String subjectId);
    List<StudentSummary> getLastTaskStudentsWithoutSubmission(String subjectId);
    List<StudentAverageGrade> getAverageGradePerStudent(String subjectId);
}
