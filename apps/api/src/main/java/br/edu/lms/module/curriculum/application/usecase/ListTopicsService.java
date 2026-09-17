package br.edu.lms.module.curriculum.application.usecase;

import br.edu.lms.module.curriculum.application.dto.TopicResponse;
import br.edu.lms.module.curriculum.domain.exception.ContentAccessDeniedException;
import br.edu.lms.module.curriculum.domain.port.in.ListTopicsUseCase;
import br.edu.lms.module.curriculum.domain.port.out.ClassroomQueryPort;
import br.edu.lms.module.curriculum.domain.port.out.SubjectRepository;
import br.edu.lms.module.curriculum.domain.port.out.TopicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListTopicsService implements ListTopicsUseCase {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomQueryPort classroomQueryPort;

    @Override
    public List<TopicResponse> execute(String subjectId, String organizationId, String requestingUserId, String requestingUserRole) {
        // Sem esta checagem, qualquer aluno da organização lia os tópicos de
        // qualquer disciplina — `/contents` já barrava, `/topics` não.
        if ("ALUNO".equals(requestingUserRole)) {
            List<String> classroomIds = subjectRepository.findClassroomIdsBySubject(subjectId);
            if (!classroomQueryPort.isMemberOfAnyClassroom(requestingUserId, classroomIds, organizationId)) {
                throw new ContentAccessDeniedException();
            }
        }

        return topicRepository.findBySubjectId(subjectId, organizationId)
                .stream()
                .map(CreateTopicService::toResponse)
                .toList();
    }
}
