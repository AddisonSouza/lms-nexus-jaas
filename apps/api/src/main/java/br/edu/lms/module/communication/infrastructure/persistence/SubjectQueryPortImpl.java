package br.edu.lms.module.communication.infrastructure.persistence;

import br.edu.lms.module.communication.domain.port.out.SubjectQueryPort;
import br.edu.lms.module.curriculum.domain.port.in.SubjectDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectQueryPortImpl implements SubjectQueryPort {

    private final SubjectDirectoryPort subjectDirectory;

    @Override
    public List<String> findClassroomIdsBySubject(String subjectId) {
        return subjectDirectory.findClassroomIdsBySubject(subjectId);
    }

    @Override
    public List<String> findTeacherUserIdsBySubject(String subjectId) {
        return subjectDirectory.findTeacherUserIdsBySubject(subjectId);
    }
}
