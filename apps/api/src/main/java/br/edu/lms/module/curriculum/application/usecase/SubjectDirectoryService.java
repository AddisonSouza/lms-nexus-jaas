package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.SubjectDirectoryPort;
import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class SubjectDirectoryService implements SubjectDirectoryPort {

    private final SubjectRepository subjectRepository;
    private final OrganizationMemberQueryPort organizationMemberQueryPort;

    @Override
    public List<String> findClassroomIdsBySubject(String subjectId) {
        return subjectRepository.findClassroomIdsBySubject(subjectId);
    }

    @Override
    public List<String> findSubjectIdsByClassrooms(Collection<String> classroomIds) {
        if (classroomIds == null || classroomIds.isEmpty()) {
            return List.of();
        }
        return subjectRepository.findSubjectIdsByClassrooms(classroomIds);
    }

    @Override
    public List<String> findTeacherUserIdsBySubject(String subjectId) {
        var memberIds = subjectRepository.findMemberIdsBySubject(subjectId);
        return organizationMemberQueryPort.findActiveUserIdsByMemberIds(memberIds);
    }

    @Override
    public boolean isTeacherOfSubject(String subjectId, String userId) {
        return findTeacherUserIdsBySubject(subjectId).contains(userId);
    }

    @Override
    public boolean isTeacherOfSubject(String subjectId, String organizationId, String userId) {
        if (!existsSubject(subjectId, organizationId)) {
            return false;
        }
        var memberIds = subjectRepository.findMemberIdsBySubject(subjectId);
        return organizationMemberQueryPort.findUserIdsByMemberIds(memberIds, organizationId).contains(userId);
    }

    @Override
    public boolean existsSubject(String subjectId, String organizationId) {
        if (subjectId == null || subjectId.isBlank()) {
            return false;
        }
        return subjectRepository.findById(SubjectId.of(subjectId), organizationId).isPresent();
    }

    @Override
    public Map<String, String> findSubjectNamesByIds(Collection<String> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Map.of();
        }
        return subjectRepository.findNamesByIdsIncludingDeleted(subjectIds);
    }
}
