package br.edu.lms.module.assessment.domain.port.out;

public interface SubjectQueryPort {
    boolean existsByIdAndTeacher(String subjectId, String organizationId, String teacherId);
    boolean existsById(String subjectId, String organizationId);
}
