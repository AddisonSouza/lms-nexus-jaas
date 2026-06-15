package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface RemoveTeacherFromSubjectUseCase {
    void execute(SubjectId subjectId, String memberId, String organizationId);
}
