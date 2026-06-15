package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectContentResponse;
import br.edu.lms.module.curriculum.application.dto.UpdateContentCommand;
import br.edu.lms.module.curriculum.domain.exception.ContentNotFoundException;
import br.edu.lms.module.curriculum.domain.port.in.UpdateContentUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdateContentService implements UpdateContentUseCase {

    private final ContentRepository contentRepository;

    @Override
    public SubjectContentResponse execute(UpdateContentCommand command) {
        var content = contentRepository.findById(command.getContentId(), command.getOrganizationId())
                .orElseThrow(() -> new ContentNotFoundException(command.getContentId()));

        var updated = content.toBuilder()
                .title(command.getTitle() != null ? command.getTitle() : content.getTitle())
                .description(command.getDescription() != null ? command.getDescription() : content.getDescription())
                .externalUrl(command.getExternalUrl() != null ? command.getExternalUrl() : content.getExternalUrl())
                .build();

        contentRepository.save(updated);
        return CreateContentService.toResponse(updated);
    }
}
