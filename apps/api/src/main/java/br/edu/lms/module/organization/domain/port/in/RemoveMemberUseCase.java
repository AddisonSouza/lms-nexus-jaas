package br.edu.lms.module.organization.domain.port.in;

public interface RemoveMemberUseCase {
    void execute(String organizationId, String userId);
}
