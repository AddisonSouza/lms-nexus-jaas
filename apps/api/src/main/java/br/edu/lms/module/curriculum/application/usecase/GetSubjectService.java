package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.GetSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetSubjectService implements GetSubjectUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse execute(SubjectId id, String organizationId) {
        var subject = subjectRepository.findById(id, organizationId)
                .orElseThrow(SubjectNotFoundException::new);

        var classroomIds = subjectRepository.findClassroomIdsBySubject(id.getValue());
        var teacherIds = subjectRepository.findMemberIdsBySubject(id.getValue());

        return CreateSubjectService.toResponse(subject, classroomIds, teacherIds);
    }
}
