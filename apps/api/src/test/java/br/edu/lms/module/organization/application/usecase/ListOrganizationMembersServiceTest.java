package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.mapper.OrganizationMemberMapper;
import br.edu.lms.module.organization.application.mapper.OrganizationMemberMapperImpl;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import br.edu.lms.shared.domain.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListOrganizationMembersServiceTest {

    static final String ORG_ID = "org-1";
    static final String OWNER_ID = "user-owner";

    @Mock OrganizationRepository organizationRepository;
    @Mock OrganizationMemberRepository memberRepository;
    @Mock UserDirectoryPort userDirectoryPort;
    @Spy OrganizationMemberMapper memberMapper = new OrganizationMemberMapperImpl();

    @InjectMocks ListOrganizationMembersService sut;

    private void givenOrganization() {
        when(organizationRepository.findById(OrganizationId.of(ORG_ID)))
                .thenReturn(Optional.of(Organization.builder()
                        .id(OrganizationId.of(ORG_ID))
                        .name("Test Org")
                        .ownerId(OWNER_ID)
                        .build()));
    }

    private OrganizationMember member(String id, String userId, MemberRole role) {
        return OrganizationMember.builder()
                .id(id)
                .organizationId(ORG_ID)
                .userId(userId)
                .role(role)
                .joinedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }

    @Test
    void shouldReturnPagedMembersWithProfilesAndTotals() {
        givenOrganization();
        when(memberRepository.searchActiveMembers(ORG_ID, null, 0, 20))
                .thenReturn(Page.of(List.of(member("m-1", "user-1", MemberRole.PROFESSOR)), 3, 0, 20));
        when(userDirectoryPort.findProfilesByIds(List.of("user-1")))
                .thenReturn(Map.of("user-1", new UserProfile("user-1", "Ana Silva", "ana@test.com")));

        var result = sut.execute(ORG_ID, null, 0, 20);

        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.number()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.content()).singleElement().satisfies(m -> {
            assertThat(m.getId()).isEqualTo("m-1");
            assertThat(m.getName()).isEqualTo("Ana Silva");
            assertThat(m.getEmail()).isEqualTo("ana@test.com");
            assertThat(m.isOwner()).isFalse();
        });
    }

    @Test
    void shouldForwardTheSearchTermToTheRepository() {
        givenOrganization();
        when(memberRepository.searchActiveMembers(ORG_ID, "ana", 0, 20))
                .thenReturn(Page.of(List.of(), 0, 0, 20));
        when(userDirectoryPort.findProfilesByIds(List.of())).thenReturn(Map.of());

        assertThat(sut.execute(ORG_ID, "ana", 0, 20).content()).isEmpty();
        verify(memberRepository).searchActiveMembers(ORG_ID, "ana", 0, 20);
    }

    @Test
    void shouldKeepRepositoryOrderInsteadOfReSorting() {
        givenOrganization();
        when(memberRepository.searchActiveMembers(ORG_ID, null, 0, 20)).thenReturn(Page.of(List.of(
                member("m-2", "user-2", MemberRole.ALUNO),
                member("m-1", "user-1", MemberRole.ALUNO)), 2, 0, 20));
        when(userDirectoryPort.findProfilesByIds(List.of("user-2", "user-1"))).thenReturn(Map.of(
                "user-1", new UserProfile("user-1", "Ana", "a@test.com"),
                "user-2", new UserProfile("user-2", "Bruno", "b@test.com")));

        // Ordenar aqui quebraria a paginação: a página já vem ordenada do banco.
        assertThat(sut.execute(ORG_ID, null, 0, 20).content())
                .extracting("name").containsExactly("Bruno", "Ana");
    }

    @Test
    void shouldFlagTheOwnerInThePagedResult() {
        givenOrganization();
        when(memberRepository.searchActiveMembers(ORG_ID, null, 0, 20))
                .thenReturn(Page.of(List.of(member("m-1", OWNER_ID, MemberRole.ADMIN_ORG)), 1, 0, 20));
        when(userDirectoryPort.findProfilesByIds(List.of(OWNER_ID)))
                .thenReturn(Map.of(OWNER_ID, new UserProfile(OWNER_ID, "Dona da Escola", "dona@test.com")));

        assertThat(sut.execute(ORG_ID, null, 0, 20).content()).singleElement()
                .extracting("owner").isEqualTo(true);
    }

    @Test
    void shouldKeepNullNameWhenTheProfileIsMissingInThePagedResult() {
        givenOrganization();
        when(memberRepository.searchActiveMembers(ORG_ID, null, 0, 20))
                .thenReturn(Page.of(List.of(member("m-1", "user-ghost", MemberRole.ALUNO)), 1, 0, 20));
        when(userDirectoryPort.findProfilesByIds(List.of("user-ghost"))).thenReturn(Map.of());

        assertThat(sut.execute(ORG_ID, null, 0, 20).content()).singleElement()
                .satisfies(m -> assertThat(m.getName()).isNull());
    }

    @Test
    void shouldRejectUnknownOrganizationWhenPaging() {
        when(organizationRepository.findById(OrganizationId.of("missing"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("missing", null, 0, 20))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
