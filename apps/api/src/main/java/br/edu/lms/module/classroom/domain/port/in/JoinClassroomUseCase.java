package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.application.dto.JoinClassroomCommand;

public interface JoinClassroomUseCase {
    ClassroomResponse execute(JoinClassroomCommand command);
}
