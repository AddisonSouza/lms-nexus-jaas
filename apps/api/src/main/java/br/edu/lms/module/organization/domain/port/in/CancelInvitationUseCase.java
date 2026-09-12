package br.edu.lms.module.organization.domain.port.in;

public interface CancelInvitationUseCase {
    void execute(String organizationId, String invitationId);
}
