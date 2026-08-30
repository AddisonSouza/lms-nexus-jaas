package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.CannotChangeOwnerRoleException;
import br.edu.lms.module.organization.domain.exception.MemberNotFoundException;
import br.edu.lms.module.organization.domain.exception.RoleNotAssignableException;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeMemberRoleServiceTest {

    static final String ORG_ID = "org-1";
    static final String OWNER_ID = "user-owner";
    static final String MEMBER_ID = "user-member";

    @Mock OrganizationRepository organizationRepository;
    @Mock OrganizationMemberRepository memberRepository;

    @InjectMocks ChangeMemberRoleService sut;

    private void givenOrganization() {
        when(organizationRepository.findById(OrganizationId.of(ORG_ID)))
                .thenReturn(Optional.of(Organization.builder()
                        .id(OrganizationId.of(ORG_ID))
                        .name("Test Org")
                        .ownerId(OWNER_ID)
                        .build()));
    }

    private void givenActiveMember(MemberRole role) {
        when(memberRepository.findActiveByOrgAndUser(ORG_ID, MEMBER_ID))
                .thenReturn(Optional.of(OrganizationMember.builder()
                        .id("member-1")
                        .organizationId(ORG_ID)
                        .userId(MEMBER_ID)
                        .role(role)
                        .build()));
    }

    @Test
    void shouldUpdateRoleOfAnActiveMember() {
        givenOrganization();
        givenActiveMember(MemberRole.ALUNO);

        sut.execute(ORG_ID, MEMBER_ID, MemberRole.PROFESSOR);

        verify(memberRepository).updateRole("member-1", MemberRole.PROFESSOR);
    }

    @Test
    void shouldRejectChangingTheOwnerRole() {
        givenOrganization();

        assertThatThrownBy(() -> sut.execute(ORG_ID, OWNER_ID, MemberRole.ALUNO))
                .isInstanceOf(CannotChangeOwnerRoleException.class);

        verify(memberRepository, never()).updateRole(any(), any());
    }

    @Test
    void shouldRejectPromotingToAdminOrg() {
        assertThatThrownBy(() -> sut.execute(ORG_ID, MEMBER_ID, MemberRole.ADMIN_ORG))
                .isInstanceOf(RoleNotAssignableException.class);

        verifyNoInteractions(organizationRepository, memberRepository);
    }

    @Test
    void shouldRejectWhenMemberIsNotInTheOrganization() {
        givenOrganization();
        when(memberRepository.findActiveByOrgAndUser(ORG_ID, MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(ORG_ID, MEMBER_ID, MemberRole.GESTOR))
                .isInstanceOf(MemberNotFoundException.class);

        verify(memberRepository, never()).updateRole(any(), any());
    }

    @Test
    void shouldRejectUnknownOrganization() {
        when(organizationRepository.findById(OrganizationId.of("missing"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("missing", MEMBER_ID, MemberRole.GESTOR))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
