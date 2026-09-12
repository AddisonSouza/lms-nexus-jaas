package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.InviteMemberCommand;
import br.edu.lms.module.organization.domain.event.MemberInvitedEvent;
import br.edu.lms.module.organization.domain.exception.AlreadyAMemberException;
import br.edu.lms.module.organization.domain.model.Invitation;
import br.edu.lms.module.organization.domain.model.InvitationId;
import br.edu.lms.module.organization.domain.model.InvitationStatus;
import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteMemberServiceTest {

    @Mock InvitationRepository invitationRepository;
    @Mock OrganizationMemberRepository memberRepository;
    @Mock Event<MemberInvitedEvent> memberInvitedEvent;

    @InjectMocks InviteMemberService sut;

    private InviteMemberCommand cmd() {
        return InviteMemberCommand.builder()
                .organizationId("org-1")
                .email("user@test.com")
                .role(MemberRole.PROFESSOR)
                .invitedBy("admin-1")
                .build();
    }

    @Test
    void shouldSaveInvitationAndFireEvent() {
        when(memberRepository.existsActiveMemberByEmail("org-1", "user@test.com")).thenReturn(false);

        sut.execute(cmd());

        var invCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invCaptor.capture());

        var saved = invCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@test.com");
        assertThat(saved.getOrganizationId()).isEqualTo("org-1");
        assertThat(saved.getRole()).isEqualTo(MemberRole.PROFESSOR);
        assertThat(saved.getStatus().name()).isEqualTo("PENDING");
        assertThat(saved.getToken()).isNotBlank();

        var eventCaptor = ArgumentCaptor.forClass(MemberInvitedEvent.class);
        verify(memberInvitedEvent).fire(eventCaptor.capture());
        assertThat(eventCaptor.getValue().email()).isEqualTo("user@test.com");
        assertThat(eventCaptor.getValue().token()).isEqualTo(saved.getToken());
    }

    @Test
    void shouldCancelThePendingInvitationBeforeIssuingANewOne() {
        var previous = Invitation.builder()
                .id(InvitationId.generate())
                .organizationId("org-1")
                .email("user@test.com")
                .role(MemberRole.PROFESSOR)
                .token("old-token")
                .status(InvitationStatus.PENDING)
                .invitedBy("admin-1")
                .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();
        when(memberRepository.existsActiveMemberByEmail("org-1", "user@test.com")).thenReturn(false);
        when(invitationRepository.findPendingByOrgAndEmail("org-1", "user@test.com")).thenReturn(List.of(previous));

        sut.execute(cmd());

        var invCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository, times(2)).save(invCaptor.capture());

        var cancelled = invCaptor.getAllValues().get(0);
        assertThat(cancelled.getToken()).isEqualTo("old-token");
        assertThat(cancelled.getStatus()).isEqualTo(InvitationStatus.CANCELLED);

        var issued = invCaptor.getAllValues().get(1);
        assertThat(issued.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(issued.getToken()).isNotEqualTo("old-token");
    }

    @Test
    void shouldThrowWhenEmailAlreadyActiveMember() {
        when(memberRepository.existsActiveMemberByEmail("org-1", "user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(cmd()))
                .isInstanceOf(AlreadyAMemberException.class);

        verify(invitationRepository, never()).save(any());
        verify(memberInvitedEvent, never()).fire(any());
    }
}
