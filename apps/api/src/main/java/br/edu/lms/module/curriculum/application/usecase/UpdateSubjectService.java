package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.application.dto.UpdateSubjectCommand;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectCode;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.UpdateSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdateSubjectService implements UpdateSubjectUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse execute(SubjectId id, UpdateSubjectCommand command) {
        var subject = subjectRepository.findById(id, command.getOrganizationId())
                .orElseThrow(SubjectNotFoundException::new);

        var updated = subject.toBuilder()
                .name(command.getName() != null ? command.getName() : subject.getName())
                .code(command.getCode() != null ? SubjectCode.of(command.getCode()) : subject.getCode())
                .description(command.getDescription() != null ? command.getDescription() : subject.getDescription())
                .workloadHours(command.getWorkloadHours() != null ? command.getWorkloadHours() : subject.getWorkloadHours())
                .updatedAt(LocalDateTime.now())
                .build();

        var saved = subjectRepository.save(updated);
        var classroomIds = subjectRepository.findClassroomIdsBySubject(id.getValue());
        var teacherIds = subjectRepository.findMemberIdsBySubject(id.getValue());

        return CreateSubjectService.toResponse(saved, classroomIds, teacherIds);
    }
}
