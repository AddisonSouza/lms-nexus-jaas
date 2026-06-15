package br.edu.lms.module.curriculum.domain.port.out;

public interface ClassroomQueryPort {
    boolean existsByIdAndOrganizationId(String classroomId, String organizationId);
    boolean isArchived(String classroomId);
}
