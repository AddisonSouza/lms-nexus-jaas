package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.domain.model.ClassroomId;

public interface GetClassroomUseCase {
    ClassroomResponse execute(ClassroomId id, String requesterId, String organizationId, String requesterRole);
}
