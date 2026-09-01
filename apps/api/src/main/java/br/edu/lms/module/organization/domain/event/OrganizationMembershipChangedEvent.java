package br.edu.lms.module.organization.domain.event;

/**
 * O vínculo de um usuário com uma organização mudou — papel alterado, membro
 * removido ou, no futuro, saída por conta própria. O papel viaja no JWT, então
 * quem cuida de sessão precisa saber para não deixar o token antigo valendo.
 */
public record OrganizationMembershipChangedEvent(String userId, String organizationId) {
}
