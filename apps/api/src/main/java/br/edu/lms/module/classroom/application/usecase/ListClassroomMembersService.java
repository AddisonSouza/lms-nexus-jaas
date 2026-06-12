package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomMemberResponse;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.port.in.ListClassroomMembersUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListClassroomMembersService implements ListClassroomMembersUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public List<ClassroomMemberResponse> execute(ClassroomId classroomId, String organizationId) {
        return classroomRepository.findMembersByClassroom(classroomId, organizationId)
                .stream()
                .map(AddClassroomMemberService::toMemberResponse)
                .toList();
    }
}
