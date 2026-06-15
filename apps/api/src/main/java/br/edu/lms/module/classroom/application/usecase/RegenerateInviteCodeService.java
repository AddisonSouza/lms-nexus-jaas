package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.model.InviteCode;
import br.edu.lms.module.classroom.domain.port.in.RegenerateInviteCodeUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RegenerateInviteCodeService implements RegenerateInviteCodeUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public ClassroomResponse execute(ClassroomId classroomId, String organizationId) {
        var classroom = classroomRepository.findById(classroomId, organizationId)
                .orElseThrow(ClassroomNotFoundException::new);

        if (classroom.getStatus() == ClassroomStatus.ARCHIVED) {
            throw new ClassroomArchivedException();
        }

        var updated = classroom.toBuilder()
                .inviteCode(InviteCode.generate())
                .build();

        var saved = classroomRepository.save(updated);
        log.info("Invite code regenerated for classroom {}", classroomId.getValue());

        return CreateClassroomService.toResponse(saved);
    }
}
