package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.OrganizationMemberResponse;
import br.edu.lms.module.organization.application.mapper.OrganizationMemberMapper;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.port.in.ListOrganizationMembersUseCase;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import br.edu.lms.shared.domain.Page;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@RequiredArgsConstructor
public class ListOrganizationMembersService implements ListOrganizationMembersUseCase {

    private static final Comparator<OrganizationMemberResponse> BY_NAME =
            Comparator.comparing(OrganizationMemberResponse::getName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserDirectoryPort userDirectoryPort;
    private final OrganizationMemberMapper memberMapper;

    @Override
    public List<OrganizationMemberResponse> execute(String organizationId) {
        var organization = organizationRepository.findById(OrganizationId.of(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        List<OrganizationMember> members = memberRepository.findActiveMembersByOrganization(organizationId);

        var profiles = userDirectoryPort.findProfilesByIds(
                members.stream().map(OrganizationMember::getUserId).toList());

        return members.stream()
                .map(m -> memberMapper.toResponse(
                        m,
                        profiles.get(m.getUserId()),
                        organization.getOwnerId().equals(m.getUserId())))
                .sorted(BY_NAME)
                .toList();
    }

    @Override
    public Page<OrganizationMemberResponse> execute(String organizationId, String search, int page, int size) {
        var organization = organizationRepository.findById(OrganizationId.of(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        var members = memberRepository.searchActiveMembers(organizationId, search, page, size);

        var profiles = userDirectoryPort.findProfilesByIds(
                members.content().stream().map(OrganizationMember::getUserId).toList());

        // A ordem vem do banco (ORDER BY u.fullName); reordenar aqui quebraria a paginação.
        var content = members.content().stream()
                .map(m -> memberMapper.toResponse(
                        m,
                        profiles.get(m.getUserId()),
                        organization.getOwnerId().equals(m.getUserId())))
                .toList();

        return new Page<>(content, members.totalElements(), members.totalPages(),
                members.number(), members.size());
    }
}
