package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.application.dto.UpdateSubjectCommand;
import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface UpdateSubjectUseCase {
    SubjectResponse execute(SubjectId id, UpdateSubjectCommand command);
}
