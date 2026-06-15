package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.AssignTeacherCommand;
import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface AssignTeacherToSubjectUseCase {
    void execute(SubjectId subjectId, AssignTeacherCommand command);
}
