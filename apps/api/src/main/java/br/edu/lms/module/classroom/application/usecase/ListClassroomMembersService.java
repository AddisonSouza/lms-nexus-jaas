package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomMemberResponse;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomMember;
import br.edu.lms.module.classroom.domain.port.in.ListClassroomMembersUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import br.edu.lms.module.classroom.domain.port.out.UserDirectoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class ListClassroomMembersService implements ListClassroomMembersUseCase {

    private final ClassroomRepository classroomRepository;
    private final UserDirectoryPort userDirectoryPort;

    @Override
    public List<ClassroomMemberResponse> execute(ClassroomId classroomId, String organizationId) {
        List<ClassroomMember> members = classroomRepository.findMembersByClassroom(classroomId, organizationId);

        Map<String, String> names = userDirectoryPort.findNamesByIds(
                members.stream().map(ClassroomMember::getUserId).toList());

        return members.stream()
                .map(m -> ClassroomMemberResponse.builder()
                        .id(m.getId())
                        .classroomId(m.getClassroomId().getValue())
                        .userId(m.getUserId())
                        .userName(names.get(m.getUserId()))
                        .role(m.getRole())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList();
    }
}
