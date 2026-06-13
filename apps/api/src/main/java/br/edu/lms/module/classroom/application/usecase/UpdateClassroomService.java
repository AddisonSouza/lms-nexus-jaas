package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.application.dto.UpdateClassroomCommand;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.port.in.UpdateClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class UpdateClassroomService implements UpdateClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public ClassroomResponse execute(ClassroomId id, UpdateClassroomCommand command) {
        var existing = classroomRepository.findById(id, command.getOrganizationId())
                .orElseThrow(ClassroomNotFoundException::new);

        var updated = existing.toBuilder()
                .name(command.getName() != null ? command.getName() : existing.getName())
                .description(command.getDescription() != null ? command.getDescription() : existing.getDescription())
                .academicPeriod(command.getAcademicPeriod() != null ? command.getAcademicPeriod() : existing.getAcademicPeriod())
                .status(command.getStatus() != null ? command.getStatus() : existing.getStatus())
                .build();

        var saved = classroomRepository.save(updated);
        log.info("Classroom updated: {}", id.getValue());

        return CreateClassroomService.toResponse(saved);
    }
}
