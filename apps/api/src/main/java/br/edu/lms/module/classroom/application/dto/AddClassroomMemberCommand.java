package br.edu.lms.module.classroom.application.dto;

import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddClassroomMemberCommand {
    private ClassroomId classroomId;
    private String userId;
    private String organizationId;
    private ClassroomMemberRole role;
}
