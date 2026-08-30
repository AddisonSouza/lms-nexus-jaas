package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.JoinClassroomCommand;
import br.edu.lms.module.classroom.application.dto.JoinClassroomResult;

public interface JoinClassroomUseCase {
    JoinClassroomResult execute(JoinClassroomCommand command);
}
