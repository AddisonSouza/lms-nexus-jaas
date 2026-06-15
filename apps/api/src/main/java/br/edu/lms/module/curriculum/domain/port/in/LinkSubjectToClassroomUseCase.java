package br.edu.lms.module.curriculum.domain.port.in;

import br.edu.lms.module.curriculum.application.dto.LinkClassroomCommand;
import br.edu.lms.module.curriculum.domain.model.SubjectId;

public interface LinkSubjectToClassroomUseCase {
    void execute(SubjectId subjectId, LinkClassroomCommand command);
}
