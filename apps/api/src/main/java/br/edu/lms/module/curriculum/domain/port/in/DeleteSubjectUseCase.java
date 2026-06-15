package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface DeleteSubjectUseCase {
    void execute(SubjectId id, String organizationId);
}
