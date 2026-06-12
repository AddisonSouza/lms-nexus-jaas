package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.domain.port.in.ListClassroomsUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListClassroomsService implements ListClassroomsUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public List<ClassroomResponse> execute(String organizationId, String requesterId, String requesterRole) {
        boolean isManager = "ADMIN_ORG".equals(requesterRole) || "GESTOR".equals(requesterRole);
        var classrooms = isManager
                ? classroomRepository.findAllByOrganization(organizationId)
                : classroomRepository.findAllByMember(requesterId, organizationId);

        return classrooms.stream()
                .map(CreateClassroomService::toResponse)
                .toList();
    }
}
