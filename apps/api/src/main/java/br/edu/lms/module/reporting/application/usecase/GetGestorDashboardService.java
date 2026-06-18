package br.edu.lms.module.reporting.application.usecase;

import br.edu.lms.module.reporting.application.dto.AtRiskStudentResponse;
import br.edu.lms.module.reporting.application.dto.ClassroomHealthResponse;
import br.edu.lms.module.reporting.application.dto.GestorDashboardResponse;
import br.edu.lms.module.reporting.domain.port.in.GetGestorDashboardUseCase;
import br.edu.lms.module.reporting.domain.port.out.GestorDashboardQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetGestorDashboardService implements GetGestorDashboardUseCase {

    private static final int AT_RISK_STUDENTS_LIMIT = 5;

    private final GestorDashboardQueryPort gestorDashboardQueryPort;

    @Override
    public GestorDashboardResponse execute(String organizationId) {
        var classroomsHealth = gestorDashboardQueryPort.getClassroomsHealth(organizationId);

        var classrooms = classroomsHealth.stream()
                .map(health -> {
                    var atRiskStudents = gestorDashboardQueryPort
                            .listAtRiskStudents(health.getClassroomId(), AT_RISK_STUDENTS_LIMIT)
                            .stream()
                            .map(student -> AtRiskStudentResponse.builder()
                                    .studentId(student.getStudentId())
                                    .studentName(student.getStudentName())
                                    .pendingCount(student.getPendingCount())
                                    .build())
                            .toList();

                    return ClassroomHealthResponse.builder()
                            .classroomId(health.getClassroomId())
                            .classroomName(health.getClassroomName())
                            .status(health.getStatus())
                            .deliveryRate(health.getDeliveryRate())
                            .averageGrade(health.getAverageGrade())
                            .atRiskStudents(atRiskStudents)
                            .build();
                })
                .toList();

        return GestorDashboardResponse.builder()
                .classrooms(classrooms)
                .build();
    }
}
