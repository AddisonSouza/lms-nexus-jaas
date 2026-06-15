package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.application.dto.UpdateTopicCommand;
import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.curriculum.domain.port.in.UpdateTopicUseCase;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class UpdateTopicService implements UpdateTopicUseCase {

    private final TopicRepository topicRepository;

    @Override
    public TopicResponse execute(UpdateTopicCommand command) {
        var topic = topicRepository.findById(command.getTopicId(), command.getOrganizationId())
                .orElseThrow(() -> new TopicNotFoundException(command.getTopicId()));

        var updated = topic.toBuilder().title(command.getTitle()).build();
        topicRepository.save(updated);

        return CreateTopicService.toResponse(updated);
    }
}
