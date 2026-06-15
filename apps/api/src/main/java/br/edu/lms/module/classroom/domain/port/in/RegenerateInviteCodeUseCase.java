package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.domain.model.ClassroomId;

public interface RegenerateInviteCodeUseCase {
    ClassroomResponse execute(ClassroomId classroomId, String organizationId);
}
