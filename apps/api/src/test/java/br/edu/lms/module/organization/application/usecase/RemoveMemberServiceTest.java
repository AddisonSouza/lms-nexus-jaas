package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.CannotRemoveOwnerException;
import br.edu.lms.module.organization.domain.exception.MemberNotFoundException;
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
class RemoveMemberServiceTest {

    @Mock OrganizationRepository organizationRepository;
    @Mock OrganizationMemberRepository memberRepository;

    @InjectMocks RemoveMemberService sut;

    private Organization org(String ownerId) {
        return Organization.builder()
                .id(OrganizationId.of("org-1"))
                .name("Test Org")
                .ownerId(ownerId)
                .build();
    }

    private OrganizationMember member(String userId) {
        return OrganizationMember.builder()
                .id("member-1")
                .organizationId("org-1")
                .userId(userId)
                .role(MemberRole.MEMBER)
                .build();
    }

    @Test
    void shouldSoftDeleteMember() {
        when(organizationRepository.findById(OrganizationId.of("org-1"))).thenReturn(Optional.of(org("owner-1")));
        when(memberRepository.findActiveByOrgAndUser("org-1", "user-1")).thenReturn(Optional.of(member("user-1")));

        sut.execute("org-1", "user-1");

        verify(memberRepository).softDelete("member-1");
    }

    @Test
    void shouldThrowWhenAttemptingToRemoveOwner() {
        when(organizationRepository.findById(OrganizationId.of("org-1"))).thenReturn(Optional.of(org("owner-1")));

        assertThatThrownBy(() -> sut.execute("org-1", "owner-1"))
                .isInstanceOf(CannotRemoveOwnerException.class);

        verify(memberRepository, never()).softDelete(any());
    }

    @Test
    void shouldThrowWhenMemberNotFound() {
        when(organizationRepository.findById(OrganizationId.of("org-1"))).thenReturn(Optional.of(org("owner-1")));
        when(memberRepository.findActiveByOrgAndUser("org-1", "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("org-1", "user-1"))
                .isInstanceOf(MemberNotFoundException.class);

        verify(memberRepository, never()).softDelete(any());
    }

    @Test
    void shouldThrowWhenOrganizationNotFound() {
        when(organizationRepository.findById(OrganizationId.of("org-1"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute("org-1", "user-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
