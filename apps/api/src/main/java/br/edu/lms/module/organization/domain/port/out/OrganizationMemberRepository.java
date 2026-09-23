package br.edu.lms.module.organization.domain.port.out;

import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.model.UserOrganization;
import br.edu.lms.shared.domain.Page;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository {
    OrganizationMember save(OrganizationMember member);
    boolean existsActiveMemberByEmail(String organizationId, String email);
    boolean existsActiveByOrgAndUser(String organizationId, String userId);
    Optional<OrganizationMember> findActiveByOrgAndUser(String organizationId, String userId);
    /**
     * Página de vínculos ativos da organização, filtrada por nome ou e-mail do
     * usuário e ordenada por nome. {@code search} nulo ou em branco não filtra.
     */
    Page<OrganizationMember> searchActiveMembers(String organizationId, String search, int page, int size);
    List<UserOrganization> findUserOrganizations(String userId);
    void updateRole(String memberId, MemberRole role);
    void softDelete(String memberId);

    /** O vínculo removido (soft delete) deste usuário na organização, se houver. */
    Optional<OrganizationMember> findRemovedByOrgAndUser(String organizationId, String userId);

    /** Traz de volta um vínculo removido: ativo de novo, com o papel dado e ingresso agora. */
    void reactivate(String memberId, MemberRole role);
}
