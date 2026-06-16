package br.edu.lms.module.communication.domain.port.out;

import java.util.List;

public interface ClassroomQueryPort {
    boolean isMember(String userId, String classroomId, String organizationId, String role);
    List<String> listMemberUserIds(String classroomId, String role);
}
