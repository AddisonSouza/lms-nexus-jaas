package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.ContentNotFoundException;
import br.edu.lms.module.curriculum.domain.port.in.DeleteContentUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.storage.domain.port.out.StoragePort;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DeleteContentService implements DeleteContentUseCase {

    private final ContentRepository contentRepository;
    private final StoragePort storagePort;

    @Override
    public void execute(String contentId, String subjectId, String organizationId) {
        var content = contentRepository.findById(contentId, organizationId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));

        if (content.getFileKey() != null) {
            try {
                storagePort.delete(content.getFileKey());
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: key={}", content.getFileKey(), e);
            }
        }

        var deleted = content.toBuilder().deletedAt(LocalDateTime.now()).build();
        contentRepository.save(deleted);

        log.info("Content soft-deleted: {}", contentId);
    }
}
