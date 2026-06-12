package br.edu.lms.module.classroom.domain.port.in;

import br.edu.lms.module.classroom.application.dto.ClassroomMemberResponse;
import br.edu.lms.module.classroom.domain.model.ClassroomId;

import java.util.List;

public interface ListClassroomMembersUseCase {
    List<ClassroomMemberResponse> execute(ClassroomId classroomId, String organizationId);
}
