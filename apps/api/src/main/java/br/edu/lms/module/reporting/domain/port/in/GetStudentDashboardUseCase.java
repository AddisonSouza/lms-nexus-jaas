package br.edu.lms.module.reporting.domain.port.in;

import br.edu.lms.module.reporting.application.dto.StudentDashboardResponse;

public interface GetStudentDashboardUseCase {
    StudentDashboardResponse execute(String studentId, String organizationId);
}
