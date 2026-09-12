package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.mapper.OrganizationInvitationMapper;
import br.edu.lms.module.organization.application.mapper.OrganizationInvitationMapperImpl;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrganizationInvitationsServiceTest {

    static final String ORG_ID = "org-1";

    @Mock InvitationRepository invitationRepository;
    @Mock UserDirectoryPort userDirectoryPort;
    @Spy OrganizationInvitationMapper invitationMapper = new OrganizationInvitationMapperImpl();

    @InjectMocks ListOrganizationInvitationsService sut;

    private Invitation invitation(String id, InvitationStatus status, Instant expiresAt, String invitedBy) {
        return Invitation.builder()
                .id(InvitationId.of(id))
                .organizationId(ORG_ID)
                .email(id + "@test.com")
                .role(MemberRole.ALUNO)
                .token("token-" + id)
                .status(status)
                .invitedBy(invitedBy)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();
    }

    private Instant inDays(int days) {
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    @Test
    void shouldReportAPendingInvitationPastItsExpiryAsExpired() {
        when(invitationRepository.findByOrganization(ORG_ID)).thenReturn(List.of(
                invitation("valid", InvitationStatus.PENDING, inDays(3), "admin-1"),
                invitation("stale", InvitationStatus.PENDING, inDays(-1), "admin-1"),
                invitation("used", InvitationStatus.USED, inDays(-1), "admin-1"),
                invitation("cancelled", InvitationStatus.CANCELLED, inDays(-1), "admin-1")));
        when(userDirectoryPort.findProfilesByIds(anyCollection())).thenReturn(Map.of());

        assertThat(sut.execute(ORG_ID))
                .extracting("id", "status")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("valid", InvitationStatus.PENDING),
                        org.assertj.core.groups.Tuple.tuple("stale", InvitationStatus.EXPIRED),
                        org.assertj.core.groups.Tuple.tuple("used", InvitationStatus.USED),
                        org.assertj.core.groups.Tuple.tuple("cancelled", InvitationStatus.CANCELLED));
    }

    @Test
    void shouldNameWhoInvitedAndToleratesAnUnknownInviter() {
        when(invitationRepository.findByOrganization(ORG_ID)).thenReturn(List.of(
                invitation("known", InvitationStatus.PENDING, inDays(3), "admin-1"),
                invitation("orphan", InvitationStatus.PENDING, inDays(3), "gone")));
        when(userDirectoryPort.findProfilesByIds(anyCollection()))
                .thenReturn(Map.of("admin-1", new UserProfile("admin-1", "Ana Admin", "ana@test.com")));

        var result = sut.execute(ORG_ID);

        assertThat(result.get(0).getInvitedByName()).isEqualTo("Ana Admin");
        assertThat(result.get(0).getEmail()).isEqualTo("known@test.com");
        assertThat(result.get(0).getRole()).isEqualTo(MemberRole.ALUNO);
        assertThat(result.get(1).getInvitedByName()).isNull();
    }

    @Test
    void shouldReturnEmptyWhenTheOrganizationHasNoInvitation() {
        when(invitationRepository.findByOrganization(ORG_ID)).thenReturn(List.of());
        when(userDirectoryPort.findProfilesByIds(anyCollection())).thenReturn(Map.of());

        assertThat(sut.execute(ORG_ID)).isEmpty();
    }
}
