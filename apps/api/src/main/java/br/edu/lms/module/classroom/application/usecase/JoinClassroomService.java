package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.application.dto.JoinClassroomCommand;
import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.classroom.domain.exception.InvalidInviteCodeException;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import br.edu.lms.module.classroom.domain.model.ClassroomMemberRole;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.port.in.JoinClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class JoinClassroomService implements JoinClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public ClassroomResponse execute(JoinClassroomCommand command) {
        var classroom = classroomRepository.findByInviteCode(command.getInviteCode())
                .orElseThrow(InvalidInviteCodeException::new);

        if (classroom.getStatus() == ClassroomStatus.ARCHIVED) {
            throw new ClassroomArchivedException();
        }

        var existing = classroomRepository.findMember(classroom.getId(), command.getUserId());
        if (existing.isPresent()) {
            log.debug("User {} already member of classroom {}", command.getUserId(), classroom.getId().getValue());
            return CreateClassroomService.toResponse(classroom);
        }

        var member = ClassroomMember.builder()
                .id(UUID.randomUUID().toString())
                .classroomId(classroom.getId())
                .userId(command.getUserId())
                .organizationId(classroom.getOrganizationId())
                .role(ClassroomMemberRole.ALUNO)
                .build();

        classroomRepository.saveMember(member);
        log.info("User {} joined classroom {} via invite code", command.getUserId(), classroom.getId().getValue());

        return CreateClassroomService.toResponse(classroom);
    }
}
