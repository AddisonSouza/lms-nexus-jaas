package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.LinkClassroomCommand;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.port.in.LinkSubjectToClassroomUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class LinkSubjectToClassroomService implements LinkSubjectToClassroomUseCase {

    private final SubjectRepository subjectRepository;
    private final ClassroomQueryPort classroomQueryPort;

    @Override
    public void execute(SubjectId subjectId, LinkClassroomCommand command) {
        subjectRepository.findById(subjectId, command.getOrganizationId())
                .orElseThrow(SubjectNotFoundException::new);

        if (!classroomQueryPort.existsByIdAndOrganizationId(command.getClassroomId(), command.getOrganizationId())) {
            throw new NotFoundException("CLASSROOM_NOT_FOUND");
        }

        if (classroomQueryPort.isArchived(command.getClassroomId())) {
            throw new WebApplicationException(
                    Response.status(422).entity(java.util.Map.of("error", "CLASSROOM_ARCHIVED")).build());
        }

        if (subjectRepository.existsSubjectClassroomLink(subjectId.getValue(), command.getClassroomId())) {
            log.info("Subject {} already linked to classroom {} — idempotent", subjectId.getValue(), command.getClassroomId());
            return;
        }

        subjectRepository.saveSubjectClassroomLink(subjectId.getValue(), command.getClassroomId());
        log.info("Subject {} linked to classroom {}", subjectId.getValue(), command.getClassroomId());
    }
}
