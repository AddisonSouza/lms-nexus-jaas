package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.Invitation;

import java.util.Optional;

public interface InvitationRepository {
    void save(Invitation invitation);
    Optional<Invitation> findByToken(String token);
    boolean existsActiveByOrgAndEmail(String organizationId, String email);
}
