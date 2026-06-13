package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.application.dto.CreateClassroomCommand;

public interface CreateClassroomUseCase {
    ClassroomResponse execute(CreateClassroomCommand command);
}
