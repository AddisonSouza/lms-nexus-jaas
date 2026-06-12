package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.AcceptInviteCommand;
import br.edu.lms.module.organization.domain.exception.AlreadyAMemberException;
import br.edu.lms.module.organization.domain.exception.InvitationAlreadyUsedException;
import br.edu.lms.module.organization.domain.exception.InvitationExpiredException;
import br.edu.lms.module.organization.domain.exception.InvitationNotFoundException;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcceptInviteServiceTest {

    @Mock InvitationRepository invitationRepository;
    @Mock OrganizationMemberRepository memberRepository;

    @InjectMocks AcceptInviteService sut;

    private Invitation pendingInvitation() {
        return Invitation.builder()
                .id(InvitationId.generate())
                .organizationId("org-1")
                .email("user@test.com")
                .role(MemberRole.PROFESSOR)
                .token("test-token")
                .status(InvitationStatus.PENDING)
                .invitedBy("admin-1")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();
    }

    private AcceptInviteCommand cmd() {
        return AcceptInviteCommand.builder().token("test-token").userId("user-1").build();
    }

    @Test
    void shouldAcceptInvitationAndCreateMember() {
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(pendingInvitation()));
        when(memberRepository.existsActiveByOrgAndUser("org-1", "user-1")).thenReturn(false);

        sut.execute(cmd());

        var memberCaptor = ArgumentCaptor.forClass(OrganizationMember.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getUserId()).isEqualTo("user-1");
        assertThat(memberCaptor.getValue().getOrganizationId()).isEqualTo("org-1");
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(MemberRole.PROFESSOR);

        var invCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invCaptor.capture());
        assertThat(invCaptor.getValue().getStatus()).isEqualTo(InvitationStatus.USED);
    }

    @Test
    void shouldThrowWhenTokenNotFound() {
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(InvitationNotFoundException.class);
    }

    @Test
    void shouldThrowWhenInvitationAlreadyUsed() {
        var used = pendingInvitation().toBuilder().status(InvitationStatus.USED).build();
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(InvitationAlreadyUsedException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenInvitationExpired() {
        var expired = pendingInvitation().toBuilder()
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(InvitationExpiredException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserAlreadyActiveMember() {
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(pendingInvitation()));
        when(memberRepository.existsActiveByOrgAndUser("org-1", "user-1")).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(AlreadyAMemberException.class);

        verify(memberRepository, never()).save(any());
    }
}
