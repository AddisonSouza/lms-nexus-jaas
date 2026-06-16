package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.TaskResponse;
import java.util.List;

public interface ListTasksUseCase {
    List<TaskResponse> execute(String organizationId, String professorId);
}
