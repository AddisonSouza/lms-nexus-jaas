package br.edu.lms.module.curriculum.domain.port.out;

import br.edu.lms.module.curriculum.domain.model.Topic;

import java.util.List;
import java.util.Optional;

public interface TopicRepository {
    Topic save(Topic topic);
    Optional<Topic> findById(String id, String organizationId);
    List<Topic> findBySubjectId(String subjectId, String organizationId);
    int maxPositionBySubjectId(String subjectId, String organizationId);
    void softDeleteBySubjectId(String subjectId, String organizationId);
}
