package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface GetSubjectUseCase {
    SubjectResponse execute(SubjectId id, String organizationId);
}
