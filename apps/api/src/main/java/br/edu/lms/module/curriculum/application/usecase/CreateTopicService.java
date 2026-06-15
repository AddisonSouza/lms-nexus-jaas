package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.CreateTopicCommand;
import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.domain.exception.SubjectNotFoundException;
import br.edu.lms.module.curriculum.domain.model.SubjectId;
import br.edu.lms.module.curriculum.domain.model.Topic;
import br.edu.lms.module.curriculum.domain.model.TopicId;
import br.edu.lms.module.curriculum.domain.port.in.CreateTopicUseCase;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateTopicService implements CreateTopicUseCase {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public TopicResponse execute(CreateTopicCommand command) {
        subjectRepository.findById(SubjectId.of(command.getSubjectId()), command.getOrganizationId())
                .orElseThrow(() -> new SubjectNotFoundException(command.getSubjectId()));

        int nextPosition = topicRepository.maxPositionBySubjectId(command.getSubjectId(), command.getOrganizationId()) + 1;

        var topic = Topic.builder()
                .id(TopicId.generate())
                .subjectId(command.getSubjectId())
                .organizationId(command.getOrganizationId())
                .title(command.getTitle())
                .position(nextPosition)
                .build();

        topicRepository.save(topic);
        log.info("Topic created: {} in subject: {}", topic.getId().getValue(), command.getSubjectId());

        return toResponse(topic);
    }

    static TopicResponse toResponse(Topic t) {
        return TopicResponse.builder()
                .id(t.getId().getValue())
                .subjectId(t.getSubjectId())
                .organizationId(t.getOrganizationId())
                .title(t.getTitle())
                .position(t.getPosition())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
