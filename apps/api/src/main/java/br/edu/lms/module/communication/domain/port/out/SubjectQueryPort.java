package br.edu.lms.module.communication.domain.port.out;

import java.util.List;

public interface SubjectQueryPort {
    List<String> findClassroomIdsBySubject(String subjectId);
    List<String> findTeacherUserIdsBySubject(String subjectId);
}
