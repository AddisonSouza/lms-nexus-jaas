package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.ReorderTopicsCommand;
import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.curriculum.domain.port.in.ReorderTopicsUseCase;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ReorderTopicsService implements ReorderTopicsUseCase {

    private final TopicRepository topicRepository;

    @Override
    public List<TopicResponse> execute(ReorderTopicsCommand command) {
        var existingTopics = topicRepository.findBySubjectId(command.getSubjectId(), command.getOrganizationId());
        var existingIds = existingTopics.stream().map(t -> t.getId().getValue()).toList();

        for (String id : command.getTopicIds()) {
            if (!existingIds.contains(id)) {
                throw new BadRequestException("Topic id does not belong to subject: " + id);
            }
        }

        List<TopicResponse> responses = new ArrayList<>();
        for (int i = 0; i < command.getTopicIds().size(); i++) {
            String topicId = command.getTopicIds().get(i);
            var topic = existingTopics.stream()
                    .filter(t -> t.getId().getValue().equals(topicId))
                    .findFirst()
                    .orElseThrow(() -> new TopicNotFoundException(topicId));

            var reordered = topic.toBuilder().position(i + 1).build();
            topicRepository.save(reordered);
            responses.add(CreateTopicService.toResponse(reordered));
        }

        return responses;
    }
}
