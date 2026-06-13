package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.domain.model.ClassroomId;

public interface DeleteClassroomUseCase {
    void execute(ClassroomId id, String organizationId);
}
