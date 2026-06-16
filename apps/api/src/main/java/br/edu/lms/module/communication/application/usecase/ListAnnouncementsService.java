package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.AnnouncementResponse;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.port.in.ListAnnouncementsUseCase;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.communication.domain.port.out.ClassroomQueryPort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListAnnouncementsService implements ListAnnouncementsUseCase {

    private final AnnouncementRepository announcementRepository;
    private final ClassroomQueryPort classroomQueryPort;

    @Override
    public List<AnnouncementResponse> execute(String classroomId, String userId, String organizationId) {
        if (!classroomQueryPort.isMember(userId, classroomId, organizationId, null)) {
            throw new UnauthorizedAnnouncementOperationException(userId, classroomId);
        }

        return announcementRepository.findByClassroomOrderByCreatedAtDesc(classroomId, organizationId)
                .stream()
                .map(PostAnnouncementService::toResponse)
                .toList();
    }
}
