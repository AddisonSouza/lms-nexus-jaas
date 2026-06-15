package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface UnlinkSubjectFromClassroomUseCase {
    void execute(SubjectId subjectId, String classroomId, String organizationId);
}
