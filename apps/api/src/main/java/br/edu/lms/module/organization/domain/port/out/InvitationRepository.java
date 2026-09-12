package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.Invitation;
import br.edu.lms.module.organization.domain.model.InvitationId;

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

    /**
     * Convites pendentes e não expirados desta organização para este e-mail.
     * Comparação sem diferenciar caixa.
     */
    List<Invitation> findPendingByOrgAndEmail(String organizationId, String email);

    /** O convite com este id, desde que pertença à organização. */
    Optional<Invitation> findByIdInOrganization(InvitationId id, String organizationId);

    /** Todos os convites da organização, em qualquer estado, do mais recente para o mais antigo. */
    List<Invitation> findByOrganization(String organizationId);
}
