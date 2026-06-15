package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.CreateSubjectCommand;
import br.edu.lms.module.curriculum.application.dto.SubjectResponse;

public interface CreateSubjectUseCase {
    SubjectResponse execute(CreateSubjectCommand command);
}
