package br.edu.lms.module.curriculum.domain.port.in;

public interface DeleteTopicUseCase {
    void execute(String topicId, String subjectId, String organizationId);
}
