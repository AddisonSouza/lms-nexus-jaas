package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.domain.port.in.ListSubjectsUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
public class ListSubjectsService implements ListSubjectsUseCase {

    private final SubjectRepository subjectRepository;
    private final ClassroomQueryPort classroomQueryPort;
    private final OrganizationMemberQueryPort organizationMemberQueryPort;

    @Override
    public List<SubjectResponse> execute(String organizationId, String requestingUserId, String requestingUserRole) {
        boolean isStudent = "ALUNO".equals(requestingUserRole);

        // Uma consulta para as turmas do aluno; o cruzamento com as turmas de
        // cada disciplina acontece em memória, sobre dados já carregados.
        Set<String> studentClassroomIds = isStudent
                ? Set.copyOf(classroomQueryPort.findClassroomIdsByUser(requestingUserId, organizationId))
                : Set.of();

        return subjectRepository.findAllByOrganizationId(organizationId).stream()
                .map(s -> {
                    var classroomIds = subjectRepository.findClassroomIdsBySubject(s.getId().getValue());
                    var teacherIds = subjectRepository.findMemberIdsBySubject(s.getId().getValue());
                    var teacherUserIds = organizationMemberQueryPort.findUserIdsByMemberIds(
                            teacherIds, organizationId);
                    return CreateSubjectService.toResponse(s, classroomIds, teacherIds, teacherUserIds);
                })
                // Disciplina sem turma vinculada não alcança nenhum aluno.
                .filter(response -> !isStudent
                        || response.getClassroomIds().stream().anyMatch(studentClassroomIds::contains))
                .toList();
    }
}
