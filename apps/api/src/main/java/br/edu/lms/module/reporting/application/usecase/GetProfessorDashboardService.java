package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.application.dto.ProfessorDashboardResponse;
import br.edu.lms.module.reporting.application.dto.StudentAverageGradeResponse;
import br.edu.lms.module.reporting.application.dto.StudentSummaryResponse;
import br.edu.lms.module.reporting.domain.exception.UnauthorizedDashboardAccessException;
import br.edu.lms.module.reporting.domain.port.in.GetProfessorDashboardUseCase;
import br.edu.lms.module.reporting.domain.port.out.ProfessorDashboardQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetProfessorDashboardService implements GetProfessorDashboardUseCase {

    private final ProfessorDashboardQueryPort professorDashboardQueryPort;

    @Override
    public ProfessorDashboardResponse execute(String subjectId, String professorId) {
        if (!professorDashboardQueryPort.isProfessorAssignedToSubject(subjectId, professorId)) {
            throw new UnauthorizedDashboardAccessException();
        }

        var studentsWithoutSubmission = professorDashboardQueryPort
                .getLastTaskStudentsWithoutSubmission(subjectId)
                .stream()
                .map(student -> StudentSummaryResponse.builder()
                        .studentId(student.getStudentId())
                        .studentName(student.getStudentName())
                        .build())
                .toList();

        var averageGradePerStudent = professorDashboardQueryPort
                .getAverageGradePerStudent(subjectId)
                .stream()
                .map(student -> StudentAverageGradeResponse.builder()
                        .studentId(student.getStudentId())
                        .studentName(student.getStudentName())
                        .averageGrade(student.getAverageGrade())
                        .build())
                .toList();

        return ProfessorDashboardResponse.builder()
                .pendingEvaluationsCount(professorDashboardQueryPort.countPendingEvaluations(subjectId))
                .lastTaskGradeDistribution(professorDashboardQueryPort.getLastTaskGradeDistribution(subjectId))
                .studentsWithoutSubmission(studentsWithoutSubmission)
                .averageGradePerStudent(averageGradePerStudent)
                .build();
    }
}
