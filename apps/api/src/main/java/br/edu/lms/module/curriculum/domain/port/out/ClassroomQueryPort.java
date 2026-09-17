package br.edu.lms.module.curriculum.domain.port.out;

import java.util.List;

public interface ClassroomQueryPort {
    boolean existsByIdAndOrganizationId(String classroomId, String organizationId);
    boolean isArchived(String classroomId);
    boolean isMemberOfAnyClassroom(String userId, List<String> classroomIds, String organizationId);

    /**
     * Turmas de que o usuário é membro na organização. Serve para filtrar a
     * listagem de disciplinas do aluno numa consulta só, em vez de checar a
     * matrícula disciplina por disciplina.
     */
    List<String> findClassroomIdsByUser(String userId, String organizationId);
}
