package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.exception.SubjectTeacherAssignmentNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.RemoveTeacherFromSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class RemoveTeacherFromSubjectService implements RemoveTeacherFromSubjectUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public void execute(SubjectId subjectId, String memberId, String organizationId) {
        subjectRepository.findById(subjectId, organizationId)
                .orElseThrow(SubjectNotFoundException::new);

        if (!subjectRepository.existsSubjectTeacherLink(subjectId.getValue(), memberId)) {
            throw new SubjectTeacherAssignmentNotFoundException();
        }

        subjectRepository.deleteSubjectTeacherLink(subjectId.getValue(), memberId);
    }
}
