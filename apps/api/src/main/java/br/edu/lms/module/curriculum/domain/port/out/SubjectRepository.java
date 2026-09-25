package br.edu.lms.module.curriculum.domain.port.out;

import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SubjectRepository {
    Subject save(Subject subject);
    Optional<Subject> findById(SubjectId id, String organizationId);
    List<Subject> findAllByOrganizationId(String organizationId);
    void softDelete(SubjectId id, String organizationId);

    boolean existsSubjectClassroomLink(String subjectId, String classroomId);
    void saveSubjectClassroomLink(String subjectId, String classroomId);
    void deleteSubjectClassroomLink(String subjectId, String classroomId);
    List<String> findClassroomIdsBySubject(String subjectId);
    List<String> findSubjectIdsByClassrooms(Collection<String> classroomIds);

    boolean existsSubjectTeacherLink(String subjectId, String memberId);
    void saveSubjectTeacherLink(String subjectId, String memberId);
    void deleteSubjectTeacherLink(String subjectId, String memberId);
    List<String> findMemberIdsBySubject(String subjectId);

    Map<String, String> findNamesByIdsIncludingDeleted(Collection<String> subjectIds);
}
