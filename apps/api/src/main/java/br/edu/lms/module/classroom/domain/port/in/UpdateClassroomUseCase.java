package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.application.dto.UpdateClassroomCommand;
import br.edu.lms.module.classroom.domain.model.ClassroomId;

public interface UpdateClassroomUseCase {
    ClassroomResponse execute(ClassroomId id, UpdateClassroomCommand command);
}
