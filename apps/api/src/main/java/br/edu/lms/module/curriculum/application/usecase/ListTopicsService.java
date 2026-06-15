package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.domain.port.in.ListTopicsUseCase;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListTopicsService implements ListTopicsUseCase {

    private final TopicRepository topicRepository;

    @Override
    public List<TopicResponse> execute(String subjectId, String organizationId) {
        return topicRepository.findBySubjectId(subjectId, organizationId)
                .stream()
                .map(CreateTopicService::toResponse)
                .toList();
    }
}
