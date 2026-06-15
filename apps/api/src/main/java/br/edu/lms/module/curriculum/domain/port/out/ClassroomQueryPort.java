package br.edu.lms.module.curriculum.domain.port.out;

import java.util.List;

public interface ClassroomQueryPort {
    boolean existsByIdAndOrganizationId(String classroomId, String organizationId);
    boolean isArchived(String classroomId);
    boolean isMemberOfAnyClassroom(String userId, List<String> classroomIds, String organizationId);
}
