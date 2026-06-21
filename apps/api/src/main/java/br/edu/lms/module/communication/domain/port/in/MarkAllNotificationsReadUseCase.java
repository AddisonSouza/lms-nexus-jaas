package br.edu.lms.module.communication.domain.port.in;

public interface MarkAllNotificationsReadUseCase {
    long execute(String userId, String organizationId);
}
