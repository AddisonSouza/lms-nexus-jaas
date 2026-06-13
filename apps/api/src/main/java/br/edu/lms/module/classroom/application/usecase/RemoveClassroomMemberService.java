package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.domain.exception.ClassroomMemberNotFoundException;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.port.in.RemoveClassroomMemberUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class RemoveClassroomMemberService implements RemoveClassroomMemberUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public void execute(ClassroomId classroomId, String userId, String organizationId) {
        classroomRepository.findMember(classroomId, userId)
                .orElseThrow(ClassroomMemberNotFoundException::new);
        classroomRepository.softDeleteMember(classroomId, userId);
        log.info("Member {} removed from classroom {}", userId, classroomId.getValue());
    }
}
