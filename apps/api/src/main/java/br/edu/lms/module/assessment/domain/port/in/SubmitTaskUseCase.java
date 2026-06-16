package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.SubmitTaskCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;

public interface SubmitTaskUseCase {
    SubmissionResponse execute(SubmitTaskCommand command);
}
