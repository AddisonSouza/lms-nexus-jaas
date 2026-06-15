package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.domain.exception.TopicNotFoundException;
import br.edu.lms.module.curriculum.domain.port.in.DeleteTopicUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class DeleteTopicService implements DeleteTopicUseCase {

    private final TopicRepository topicRepository;
    private final ContentRepository contentRepository;

    @Override
    @Transactional
    public void execute(String topicId, String subjectId, String organizationId) {
        var topic = topicRepository.findById(topicId, organizationId)
                .orElseThrow(() -> new TopicNotFoundException(topicId));

        contentRepository.softDeleteByTopicId(topicId, organizationId);

        var deleted = topic.toBuilder().deletedAt(LocalDateTime.now()).build();
        topicRepository.save(deleted);

        log.info("Topic soft-deleted: {} (with cascade content delete)", topicId);
    }
}
