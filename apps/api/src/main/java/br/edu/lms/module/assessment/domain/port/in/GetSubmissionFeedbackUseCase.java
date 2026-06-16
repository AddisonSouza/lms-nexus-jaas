package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.SubmissionResponse;

public interface GetSubmissionFeedbackUseCase {
    SubmissionResponse execute(String submissionId, String studentId, String organizationId);
}
