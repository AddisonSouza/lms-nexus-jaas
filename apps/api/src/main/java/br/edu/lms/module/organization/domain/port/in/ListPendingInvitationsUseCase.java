package br.edu.lms.module.organization.domain.port.in;

import br.edu.lms.module.organization.application.dto.PendingInvitationResponse;

import java.util.List;

public interface ListPendingInvitationsUseCase {

    /**
     * Convites pendentes endereçados ao e-mail do usuário autenticado.
     * Lista vazia quando o usuário é desconhecido ou não há convite.
     */
    List<PendingInvitationResponse> execute(String userId);
}
