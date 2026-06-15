package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.SubjectContentsGroupedResponse;
import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.domain.exception.ContentAccessDeniedException;
import br.edu.lms.module.curriculum.domain.port.in.ListSubjectContentsUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.ContentRepository;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListSubjectContentsService implements ListSubjectContentsUseCase {

    private final TopicRepository topicRepository;
    private final ContentRepository contentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomQueryPort classroomQueryPort;

    @Override
    public SubjectContentsGroupedResponse execute(String subjectId, String organizationId, String requestingUserId, String requestingUserRole) {
        if ("ALUNO".equals(requestingUserRole)) {
            List<String> classroomIds = subjectRepository.findClassroomIdsBySubject(subjectId);
            boolean hasAccess = classroomQueryPort.isMemberOfAnyClassroom(requestingUserId, classroomIds, organizationId);
            if (!hasAccess) {
                throw new ContentAccessDeniedException();
            }
        }

        var topics = topicRepository.findBySubjectId(subjectId, organizationId);

        var grouped = topics.stream()
                .map(topic -> {
                    var contents = contentRepository.findByTopicId(topic.getId().getValue(), organizationId)
                            .stream()
                            .map(CreateContentService::toResponse)
                            .toList();

                    TopicResponse topicResponse = CreateTopicService.toResponse(topic);
                    return SubjectContentsGroupedResponse.TopicWithContents.builder()
                            .topic(topicResponse)
                            .contents(contents)
                            .build();
                })
                .toList();

        return SubjectContentsGroupedResponse.builder().topics(grouped).build();
    }
}
