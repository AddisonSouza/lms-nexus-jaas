package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.SubmissionResponse;

import java.util.List;

public interface ListTaskSubmissionsUseCase {
    List<SubmissionResponse> execute(String taskId, String professorId, String organizationId);
}
