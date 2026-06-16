package br.edu.lms.module.communication.domain.port.out;

public interface ClassroomQueryPort {
    boolean isMember(String userId, String classroomId, String organizationId, String role);
}
