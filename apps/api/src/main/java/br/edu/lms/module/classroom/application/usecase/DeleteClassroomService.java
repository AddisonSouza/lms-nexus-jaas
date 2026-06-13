package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.port.in.DeleteClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DeleteClassroomService implements DeleteClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public void execute(ClassroomId id, String organizationId) {
        classroomRepository.findById(id, organizationId)
                .orElseThrow(ClassroomNotFoundException::new);
        classroomRepository.softDelete(id, organizationId);
        log.info("Classroom soft-deleted: {}", id.getValue());
    }
}
