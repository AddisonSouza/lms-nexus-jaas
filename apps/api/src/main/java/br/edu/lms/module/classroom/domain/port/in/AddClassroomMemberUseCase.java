package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.AddClassroomMemberCommand;
import br.edu.lms.module.classroom.application.dto.ClassroomMemberResponse;

public interface AddClassroomMemberUseCase {
    ClassroomMemberResponse execute(AddClassroomMemberCommand command);
}
