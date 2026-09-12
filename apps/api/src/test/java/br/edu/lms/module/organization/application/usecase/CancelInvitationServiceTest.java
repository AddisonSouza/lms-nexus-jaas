package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.domain.exception.InvitationNotFoundException;
import br.edu.lms.module.organization.domain.exception.InvitationNotPendingException;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
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
class CancelInvitationServiceTest {

    static final String ORG_ID = "org-1";
    static final String INVITATION_ID = "inv-1";

    @Mock InvitationRepository invitationRepository;

    @InjectMocks CancelInvitationService sut;

    private Invitation invitation(InvitationStatus status, int daysToExpiry) {
        return Invitation.builder()
                .id(InvitationId.of(INVITATION_ID))
                .organizationId(ORG_ID)
                .email("guest@test.com")
                .role(MemberRole.ALUNO)
                .token("token-1")
                .status(status)
                .invitedBy("admin-1")
                .expiresAt(Instant.now().plus(daysToExpiry, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .build();
    }

    private void given(Invitation invitation) {
        when(invitationRepository.findByIdInOrganization(InvitationId.of(INVITATION_ID), ORG_ID))
                .thenReturn(Optional.ofNullable(invitation));
    }

    @Test
    void shouldCancelAPendingInvitation() {
        given(invitation(InvitationStatus.PENDING, 3));

        sut.execute(ORG_ID, INVITATION_ID);

        var captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InvitationStatus.CANCELLED);
        assertThat(captor.getValue().getToken()).isEqualTo("token-1");
    }

    @Test
    void shouldAnswerNotFoundForAnInvitationOutsideTheOrganization() {
        given(null);

        assertThatThrownBy(() -> sut.execute(ORG_ID, INVITATION_ID))
                .isInstanceOf(InvitationNotFoundException.class);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToCancelAnAcceptedInvitation() {
        given(invitation(InvitationStatus.USED, 3));

        assertThatThrownBy(() -> sut.execute(ORG_ID, INVITATION_ID))
                .isInstanceOf(InvitationNotPendingException.class);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToCancelAnExpiredInvitation() {
        given(invitation(InvitationStatus.PENDING, -1));

        assertThatThrownBy(() -> sut.execute(ORG_ID, INVITATION_ID))
                .isInstanceOf(InvitationNotPendingException.class);
        verify(invitationRepository, never()).save(any());
    }

    @Test
    void shouldRefuseToCancelTwice() {
        given(invitation(InvitationStatus.CANCELLED, 3));

        assertThatThrownBy(() -> sut.execute(ORG_ID, INVITATION_ID))
                .isInstanceOf(InvitationNotPendingException.class);
    }
}
