package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.TaskResponse;

public interface PublishTaskUseCase {
    TaskResponse execute(String taskId, String organizationId, String requestingUserId);
}
