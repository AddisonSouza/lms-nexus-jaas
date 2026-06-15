package br.edu.lms.module.curriculum.domain.port.in;

public interface DeleteContentUseCase {
    void execute(String contentId, String subjectId, String organizationId);
}
