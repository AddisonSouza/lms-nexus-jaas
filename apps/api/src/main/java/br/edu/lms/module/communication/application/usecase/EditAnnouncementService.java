package br.edu.lms.module.communication.application.usecase;

import br.edu.lms.module.communication.application.dto.AnnouncementResponse;
import br.edu.lms.module.communication.application.dto.EditAnnouncementCommand;
import br.edu.lms.module.communication.domain.exception.AnnouncementNotFoundException;
import br.edu.lms.module.communication.domain.exception.EmptyContentException;
import br.edu.lms.module.communication.domain.exception.UnauthorizedAnnouncementOperationException;
import br.edu.lms.module.communication.domain.model.AnnouncementId;
import br.edu.lms.module.communication.domain.port.in.EditAnnouncementUseCase;
import br.edu.lms.module.communication.domain.port.out.AnnouncementRepository;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class EditAnnouncementService implements EditAnnouncementUseCase {

    private final AnnouncementRepository announcementRepository;
    private final StoragePort storagePort;

    @Override
    public AnnouncementResponse execute(EditAnnouncementCommand command) {
        var announcement = announcementRepository.findById(AnnouncementId.of(command.getAnnouncementId()))
                .orElseThrow(() -> new AnnouncementNotFoundException(command.getAnnouncementId()));

        if (!announcement.isAuthoredBy(command.getUserId())) {
            throw new UnauthorizedAnnouncementOperationException(command.getUserId(), command.getAnnouncementId());
        }

        if (command.getContent() == null || command.getContent().isBlank()) {
            throw new EmptyContentException();
        }

        var builder = announcement.toBuilder().content(command.getContent());
        if (command.getAttachments() != null) {
            builder.attachments(PostAnnouncementService.buildAttachments(command.getAttachments(), storagePort));
        }

        var saved = announcementRepository.save(builder.build());
        log.info("Announcement edited: id={} author={}", saved.getId().getValue(), saved.getAuthorId());
        return PostAnnouncementService.toResponse(saved);
    }
}
