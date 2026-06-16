package br.edu.lms.module.communication.domain.port.in;

public interface DeleteAnnouncementUseCase {
    void execute(String announcementId, String userId, String organizationId);
}
