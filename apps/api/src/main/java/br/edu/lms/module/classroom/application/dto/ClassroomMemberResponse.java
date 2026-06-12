package br.edu.lms.module.classroom.application.dto;

import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClassroomMemberResponse {
    private String id;
    private String classroomId;
    private String userId;
    private ClassroomMemberRole role;
    private LocalDateTime joinedAt;
}
