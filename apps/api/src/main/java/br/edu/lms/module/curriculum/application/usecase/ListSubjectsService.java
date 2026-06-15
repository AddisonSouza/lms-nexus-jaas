package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.domain.port.in.ListSubjectsUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListSubjectsService implements ListSubjectsUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public List<SubjectResponse> execute(String organizationId) {
        return subjectRepository.findAllByOrganizationId(organizationId).stream()
                .map(s -> {
                    var classroomIds = subjectRepository.findClassroomIdsBySubject(s.getId().getValue());
                    var teacherIds = subjectRepository.findMemberIdsBySubject(s.getId().getValue());
                    return CreateSubjectService.toResponse(s, classroomIds, teacherIds);
                })
                .toList();
    }
}
