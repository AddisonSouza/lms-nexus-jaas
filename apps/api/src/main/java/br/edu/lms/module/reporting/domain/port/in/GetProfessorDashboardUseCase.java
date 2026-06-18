package br.edu.lms.module.reporting.domain.port.in;

import br.edu.lms.module.reporting.application.dto.ProfessorDashboardResponse;

public interface GetProfessorDashboardUseCase {
    ProfessorDashboardResponse execute(String subjectId, String professorId);
}
