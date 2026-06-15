package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.AssignTeacherCommand;
import br.edu.lms.module.curriculum.domain.exception.InvalidTeacherAssignmentException;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.AssignTeacherToSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.OrganizationMemberQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AssignTeacherToSubjectService implements AssignTeacherToSubjectUseCase {

    private final SubjectRepository subjectRepository;
    private final OrganizationMemberQueryPort memberQueryPort;

    @Override
    public void execute(SubjectId subjectId, AssignTeacherCommand command) {
        subjectRepository.findById(subjectId, command.getOrganizationId())
                .orElseThrow(SubjectNotFoundException::new);

        if (!memberQueryPort.existsByIdAndOrganizationId(command.getMemberId(), command.getOrganizationId())) {
            throw new InvalidTeacherAssignmentException("MEMBER_NOT_IN_ORGANIZATION");
        }

        if (!memberQueryPort.hasProfessorRole(command.getMemberId(), command.getOrganizationId())) {
            throw new InvalidTeacherAssignmentException("MEMBER_NOT_A_PROFESSOR");
        }

        if (subjectRepository.existsSubjectTeacherLink(subjectId.getValue(), command.getMemberId())) {
            log.info("Teacher {} already assigned to subject {} — idempotent", command.getMemberId(), subjectId.getValue());
            return;
        }

        subjectRepository.saveSubjectTeacherLink(subjectId.getValue(), command.getMemberId());
        log.info("Teacher {} assigned to subject {}", command.getMemberId(), subjectId.getValue());
    }
}
