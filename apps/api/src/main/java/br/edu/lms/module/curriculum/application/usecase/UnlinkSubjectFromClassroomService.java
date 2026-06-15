package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.SubjectClassroomLinkNotFoundException;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.UnlinkSubjectFromClassroomUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UnlinkSubjectFromClassroomService implements UnlinkSubjectFromClassroomUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public void execute(SubjectId subjectId, String classroomId, String organizationId) {
        subjectRepository.findById(subjectId, organizationId)
                .orElseThrow(SubjectNotFoundException::new);

        if (!subjectRepository.existsSubjectClassroomLink(subjectId.getValue(), classroomId)) {
            throw new SubjectClassroomLinkNotFoundException();
        }

        subjectRepository.deleteSubjectClassroomLink(subjectId.getValue(), classroomId);
    }
}
