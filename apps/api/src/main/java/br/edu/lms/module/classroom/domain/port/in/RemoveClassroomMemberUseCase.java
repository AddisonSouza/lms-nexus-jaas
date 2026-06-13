package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.domain.model.ClassroomId;

public interface RemoveClassroomMemberUseCase {
    void execute(ClassroomId classroomId, String userId, String organizationId);
}
