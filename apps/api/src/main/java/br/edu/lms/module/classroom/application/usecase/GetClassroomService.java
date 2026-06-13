package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.domain.exception.ClassroomNotFoundException;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.port.in.GetClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class GetClassroomService implements GetClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    @Override
    public ClassroomResponse execute(ClassroomId id, String requesterId, String organizationId, String requesterRole) {
        var classroom = classroomRepository.findById(id, organizationId)
                .orElseThrow(ClassroomNotFoundException::new);

        boolean isManager = "ADMIN_ORG".equals(requesterRole) || "GESTOR".equals(requesterRole);
        if (!isManager) {
            boolean isMember = classroomRepository.findMember(id, requesterId).isPresent();
            if (!isMember) {
                throw new ClassroomNotFoundException();
            }
        }

        var response = CreateClassroomService.toResponse(classroom);
        if (!isManager) {
            return ClassroomResponse.builder()
                    .id(response.getId())
                    .name(response.getName())
                    .description(response.getDescription())
                    .academicPeriod(response.getAcademicPeriod())
                    .status(response.getStatus())
                    .organizationId(response.getOrganizationId())
                    .createdAt(response.getCreatedAt())
                    .build();
        }
        return response;
    }
}
