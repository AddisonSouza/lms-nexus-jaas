package br.edu.lms.module.assessment.domain.port.in;

import br.edu.lms.module.assessment.application.dto.EditSubmissionCommand;
import br.edu.lms.module.assessment.application.dto.SubmissionResponse;

public interface EditSubmissionUseCase {
    SubmissionResponse execute(EditSubmissionCommand command);
}
