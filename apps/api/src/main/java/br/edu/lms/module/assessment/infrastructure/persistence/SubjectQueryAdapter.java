package br.edu.lms.module.assessment.infrastructure.persistence;

import br.edu.lms.module.assessment.domain.port.out.SubjectQueryPort;
import br.edu.lms.module.curriculum.domain.port.in.SubjectDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectQueryAdapter implements SubjectQueryPort {

    private final SubjectDirectoryPort subjectDirectory;

    @Override
    public boolean existsByIdAndTeacher(String subjectId, String organizationId, String teacherId) {
        return subjectDirectory.isTeacherOfSubject(subjectId, organizationId, teacherId);
    }

    @Override
    public boolean existsById(String subjectId, String organizationId) {
        return subjectDirectory.existsSubject(subjectId, organizationId);
    }
}
