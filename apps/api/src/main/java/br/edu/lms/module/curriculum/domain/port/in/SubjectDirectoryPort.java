package br.edu.lms.module.curriculum.domain.port.in;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Leitura dos vínculos de disciplina para os outros módulos. Quem precisa saber
 * que turmas uma disciplina atende ou quem a leciona pergunta aqui, em vez de
 * consultar as tabelas do curriculum (MOD-03, MOD-05).
 */
public interface SubjectDirectoryPort {

    List<String> findClassroomIdsBySubject(String subjectId);

    List<String> findSubjectIdsByClassrooms(Collection<String> classroomIds);

    /** userIds dos professores vinculados, só de membros ativos. */
    List<String> findTeacherUserIdsBySubject(String subjectId);

    /** O usuário leciona a disciplina como membro ativo, em qualquer organização. */
    boolean isTeacherOfSubject(String subjectId, String userId);

    /** Como {@link #isTeacherOfSubject}, mas exige disciplina ativa e membro da mesma organização. */
    boolean isTeacherOfSubject(String subjectId, String organizationId, String userId);

    boolean existsSubject(String subjectId, String organizationId);

    /** Nome por id, incluindo disciplinas excluídas — histórico de notas ainda as exibe. */
    Map<String, String> findSubjectNamesByIds(Collection<String> subjectIds);
}
