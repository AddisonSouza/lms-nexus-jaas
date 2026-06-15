package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.CreateSubjectCommand;
import br.edu.lms.module.curriculum.application.dto.SubjectResponse;
import br.edu.lms.module.curriculum.domain.model.Subject;
import br.edu.lms.module.curriculum.domain.model.SubjectCode;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.CreateSubjectUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateSubjectService implements CreateSubjectUseCase {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponse execute(CreateSubjectCommand command) {
        var subject = Subject.builder()
                .id(SubjectId.generate())
                .name(command.getName())
                .code(SubjectCode.of(command.getCode()))
                .description(command.getDescription())
                .workloadHours(command.getWorkloadHours())
                .organizationId(command.getOrganizationId())
                .createdAt(LocalDateTime.now())
                .build();

        var saved = subjectRepository.save(subject);
        log.info("Subject created: {} in org: {}", saved.getId().getValue(), command.getOrganizationId());

        return toResponse(saved, List.of(), List.of());
    }

    static SubjectResponse toResponse(Subject s, List<String> classroomIds, List<String> teacherMemberIds) {
        return SubjectResponse.builder()
                .id(s.getId().getValue())
                .name(s.getName())
                .code(s.getCode() != null ? s.getCode().getValue() : null)
                .description(s.getDescription())
                .workloadHours(s.getWorkloadHours())
                .organizationId(s.getOrganizationId())
                .classroomIds(classroomIds)
                .teacherMemberIds(teacherMemberIds)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
