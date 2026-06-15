package br.edu.lms.module.curriculum.domain.port.out;

import br.edu.lms.module.curriculum.domain.model.SubjectContent;

import java.util.List;
import java.util.Optional;

public interface ContentRepository {
    SubjectContent save(SubjectContent content);
    Optional<SubjectContent> findById(String id, String organizationId);
    List<SubjectContent> findByTopicId(String topicId, String organizationId);
    List<SubjectContent> findBySubjectId(String subjectId, String organizationId);
    int maxPositionByTopicId(String topicId, String organizationId);
    void softDeleteByTopicId(String topicId, String organizationId);
}
