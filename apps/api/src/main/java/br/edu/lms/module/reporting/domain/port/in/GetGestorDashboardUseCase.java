package br.edu.lms.module.reporting.domain.port.in;

import br.edu.lms.module.reporting.application.dto.GestorDashboardResponse;

public interface GetGestorDashboardUseCase {
    GestorDashboardResponse execute(String organizationId);
}
