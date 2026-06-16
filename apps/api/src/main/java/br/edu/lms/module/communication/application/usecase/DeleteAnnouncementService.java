package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.domain.exception.AnnouncementNotFoundException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.in.DeleteAnnouncementUseCase;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DeleteAnnouncementService implements DeleteAnnouncementUseCase {

    private final AnnouncementRepository announcementRepository;

    @Override
    public void execute(String announcementId, String userId, String organizationId) {
        var announcement = announcementRepository.findById(AnnouncementId.of(announcementId))
                .orElseThrow(() -> new AnnouncementNotFoundException(announcementId));

        if (!announcement.isAuthoredBy(userId)) {
            throw new UnauthorizedAnnouncementOperationException(userId, announcementId);
        }

        var deleted = announcement.toBuilder().deletedAt(LocalDateTime.now()).build();
        announcementRepository.save(deleted);
        log.info("Announcement deleted: id={} author={}", announcementId, userId);
    }
}
