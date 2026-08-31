package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.Invitation;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository {
    void save(Invitation invitation);
    Optional<Invitation> findByToken(String token);
    boolean existsActiveByOrgAndEmail(String organizationId, String email);

    /**
     * Convites pendentes e não expirados endereçados a este e-mail, do mais
     * recente para o mais antigo. Comparação sem diferenciar caixa.
     */
    List<Invitation> findPendingByEmail(String email);
}
