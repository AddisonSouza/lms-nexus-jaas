package br.edu.lms.module.reporting.domain.port.out;

import br.edu.lms.module.reporting.domain.model.AtRiskStudent;
import br.edu.lms.module.reporting.domain.model.ClassroomHealth;

import java.util.List;

public interface GestorDashboardQueryPort {
    List<ClassroomHealth> getClassroomsHealth(String organizationId);
    List<AtRiskStudent> listAtRiskStudents(String classroomId, int limit);
}
