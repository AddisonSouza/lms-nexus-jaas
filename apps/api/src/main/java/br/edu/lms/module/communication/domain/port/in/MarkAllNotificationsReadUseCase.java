package br.edu.lms.module.communication.domain.port.in;

public interface MarkAllNotificationsReadUseCase {
    void execute(String userId, String organizationId);
}
