package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.CreateTaskCommand;
import br.edu.lms.module.assessment.application.dto.TaskResponse;

public interface CreateTaskUseCase {
    TaskResponse execute(CreateTaskCommand command);
}
