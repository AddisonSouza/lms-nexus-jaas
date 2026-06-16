package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.EvaluateSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;

public interface EvaluateSubmissionUseCase {
    SubmissionResponse execute(EvaluateSubmissionCommand command);
}
