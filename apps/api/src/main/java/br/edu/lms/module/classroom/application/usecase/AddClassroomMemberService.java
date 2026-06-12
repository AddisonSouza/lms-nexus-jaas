package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.AddClassroomMemberCommand;
import br.edu.lms.module.classroom.application.dto.ClassroomMemberResponse;
import br.edu.lms.module.classroom.domain.exception.ClassroomArchivedException;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.exception.MemberNotInOrganizationException;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.port.in.AddClassroomMemberUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class AddClassroomMemberService implements AddClassroomMemberUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public ClassroomMemberResponse execute(AddClassroomMemberCommand command) {
        var classroom = classroomRepository.findById(command.getClassroomId(), command.getOrganizationId())
                .orElseThrow(ClassroomNotFoundException::new);

        if (classroom.getStatus() == ClassroomStatus.ARCHIVED) {
            throw new ClassroomArchivedException();
        }

        if (!classroomRepository.isUserInOrganization(command.getUserId(), command.getOrganizationId())) {
            throw new MemberNotInOrganizationException();
        }

        var existing = classroomRepository.findMember(command.getClassroomId(), command.getUserId());
        if (existing.isPresent()) {
            return toMemberResponse(existing.get());
        }

        var member = ClassroomMember.builder()
                .id(UUID.randomUUID().toString())
                .classroomId(command.getClassroomId())
                .userId(command.getUserId())
                .organizationId(command.getOrganizationId())
                .role(command.getRole())
                .build();

        var saved = classroomRepository.saveMember(member);
        log.info("Member {} added to classroom {}", command.getUserId(), command.getClassroomId().getValue());

        return toMemberResponse(saved);
    }

    static ClassroomMemberResponse toMemberResponse(ClassroomMember m) {
        return ClassroomMemberResponse.builder()
                .id(m.getId())
                .classroomId(m.getClassroomId().getValue())
                .userId(m.getUserId())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
