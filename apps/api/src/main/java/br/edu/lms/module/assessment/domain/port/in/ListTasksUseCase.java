package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.TaskSummaryResponse;
import java.util.List;

public interface ListTasksUseCase {
    List<TaskSummaryResponse> execute(String organizationId, String professorId);
}
