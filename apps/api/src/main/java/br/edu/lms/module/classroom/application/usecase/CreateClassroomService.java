package br.edu.lms.module.classroom.application.usecase;

import br.edu.lms.module.classroom.application.dto.ClassroomResponse;
import br.edu.lms.module.classroom.application.dto.CreateClassroomCommand;
import br.edu.lms.module.classroom.domain.model.Classroom;
import br.edu.lms.module.classroom.domain.model.ClassroomId;
import br.edu.lms.module.classroom.domain.model.ClassroomStatus;
import br.edu.lms.module.classroom.domain.model.InviteCode;
import br.edu.lms.module.classroom.domain.port.in.CreateClassroomUseCase;
import br.edu.lms.module.classroom.domain.port.out.ClassroomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateClassroomService implements CreateClassroomUseCase {

    private final ClassroomRepository classroomRepository;

    /**
     * Papéis que enxergam o código de convite da turma. O ALUNO entra pelo código,
     * mas nunca o recebe de volta na listagem nem no detalhe (RF-08).
     */
    private static final Set<String> ROLES_WITH_INVITE_CODE = Set.of("ADMIN_ORG", "GESTOR", "PROFESSOR");

    @Override
    public ClassroomResponse execute(CreateClassroomCommand command) {
        var classroom = Classroom.builder()
                .id(ClassroomId.generate())
                .name(command.getName())
                .description(command.getDescription())
                .academicPeriod(command.getAcademicPeriod())
                .status(ClassroomStatus.ACTIVE)
                .inviteCode(InviteCode.generate())
                .organizationId(command.getOrganizationId())
                .createdAt(LocalDateTime.now())
                .build();

        var saved = classroomRepository.save(classroom);
        log.info("Classroom created: {} in org: {}", saved.getId().getValue(), command.getOrganizationId());

        return toResponse(saved);
    }

    /** Mapeia a turma escondendo o código de convite de quem não pode vê-lo. */
    static ClassroomResponse toResponse(Classroom c, String requesterRole) {
        var response = toResponse(c);
        if (ROLES_WITH_INVITE_CODE.contains(requesterRole)) {
            return response;
        }
        return response.toBuilder().inviteCode(null).build();
    }

    static ClassroomResponse toResponse(Classroom c) {
        return ClassroomResponse.builder()
                .id(c.getId().getValue())
                .name(c.getName())
                .description(c.getDescription())
                .academicPeriod(c.getAcademicPeriod())
                .status(c.getStatus())
                .inviteCode(c.getInviteCode().getValue())
                .organizationId(c.getOrganizationId())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
