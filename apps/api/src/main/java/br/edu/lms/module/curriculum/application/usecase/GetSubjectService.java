package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.domain.exception.ContentAccessDeniedException;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.GetSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetSubjectService implements GetSubjectUseCase {

    private final SubjectRepository subjectRepository;
    private final ClassroomQueryPort classroomQueryPort;
    private final OrganizationMemberQueryPort organizationMemberQueryPort;

    @Override
    public SubjectResponse execute(SubjectId id, String organizationId, String requestingUserId, String requestingUserRole) {
        var subject = subjectRepository.findById(id, organizationId)
                .orElseThrow(SubjectNotFoundException::new);

        var classroomIds = subjectRepository.findClassroomIdsBySubject(id.getValue());

        // Mesma regra de ListSubjectContentsService: o aluno só alcança a
        // disciplina se estiver matriculado em alguma turma vinculada a ela.
        if ("ALUNO".equals(requestingUserRole)
                && !classroomQueryPort.isMemberOfAnyClassroom(requestingUserId, classroomIds, organizationId)) {
            throw new ContentAccessDeniedException();
        }

        var teacherIds = subjectRepository.findMemberIdsBySubject(id.getValue());
        var teacherUserIds = organizationMemberQueryPort.findUserIdsByMemberIds(teacherIds, organizationId);

        return CreateSubjectService.toResponse(subject, classroomIds, teacherIds, teacherUserIds);
    }
}
