package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.mapper.PendingInvitationMapper;
import br.edu.lms.module.organization.application.mapper.PendingInvitationMapperImpl;
import br.edu.lms.module.organization.domain.model.*;
import br.edu.lms.module.organization.domain.port.out.InvitationRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import br.edu.lms.module.organization.domain.port.out.UserDirectoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPendingInvitationsServiceTest {

    static final String USER_ID = "user-1";
    static final String EMAIL = "convidado@test.com";
    static final String ORG_ID = "org-1";

    @Mock InvitationRepository invitationRepository;
    @Mock OrganizationRepository organizationRepository;
    @Mock UserDirectoryPort userDirectoryPort;
    @Spy PendingInvitationMapper pendingInvitationMapper = new PendingInvitationMapperImpl();

    @InjectMocks ListPendingInvitationsService sut;

    private Invitation invitation(String token, String organizationId) {
        return Invitation.builder()
                .id(InvitationId.of("inv-" + token))
                .organizationId(organizationId)
                .email(EMAIL)
                .role(MemberRole.PROFESSOR)
                .token(token)
                .status(InvitationStatus.PENDING)
                .invitedBy("admin-1")
                .expiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();
    }

    private void givenOrganization(String id, String name) {
        when(organizationRepository.findById(OrganizationId.of(id)))
                .thenReturn(Optional.of(Organization.builder()
                        .id(OrganizationId.of(id))
                        .name(name)
                        .ownerId("owner-1")
                        .build()));
    }

    @Test
    void shouldReturnThePendingInvitationsOfTheAuthenticatedUserEmail() {
        when(userDirectoryPort.findEmailById(USER_ID)).thenReturn(Optional.of(EMAIL));
        when(invitationRepository.findPendingByEmail(EMAIL))
                .thenReturn(List.of(invitation("tok-1", ORG_ID)));
        givenOrganization(ORG_ID, "Escola Alfa");

        var result = sut.execute(USER_ID);

        assertThat(result).singleElement().satisfies(i -> {
            assertThat(i.getToken()).isEqualTo("tok-1");
            assertThat(i.getOrganizationId()).isEqualTo(ORG_ID);
            assertThat(i.getOrganizationName()).isEqualTo("Escola Alfa");
            assertThat(i.getRole()).isEqualTo(MemberRole.PROFESSOR);
            assertThat(i.getExpiresAt()).isNotNull();
        });
    }

    @Test
    void shouldKeepTheRepositoryOrder() {
        when(userDirectoryPort.findEmailById(USER_ID)).thenReturn(Optional.of(EMAIL));
        when(invitationRepository.findPendingByEmail(EMAIL))
                .thenReturn(List.of(invitation("recente", ORG_ID), invitation("antigo", "org-2")));
        givenOrganization(ORG_ID, "Escola Alfa");
        givenOrganization("org-2", "Escola Beta");

        assertThat(sut.execute(USER_ID))
                .extracting("token")
                .containsExactly("recente", "antigo");
    }

    @Test
    void shouldReturnEmptyWhenTheUserHasNoInvitation() {
        when(userDirectoryPort.findEmailById(USER_ID)).thenReturn(Optional.of(EMAIL));
        when(invitationRepository.findPendingByEmail(EMAIL)).thenReturn(List.of());

        assertThat(sut.execute(USER_ID)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForAnUnknownUserWithoutTouchingTheRepository() {
        when(userDirectoryPort.findEmailById("ghost")).thenReturn(Optional.empty());

        assertThat(sut.execute("ghost")).isEmpty();
        verifyNoInteractions(invitationRepository);
    }

    @Test
    void shouldSkipAnInvitationWhoseOrganizationIsGoneInsteadOfFailing() {
        when(userDirectoryPort.findEmailById(USER_ID)).thenReturn(Optional.of(EMAIL));
        when(invitationRepository.findPendingByEmail(EMAIL))
                .thenReturn(List.of(invitation("orfao", "org-sumida"), invitation("bom", ORG_ID)));
        when(organizationRepository.findById(OrganizationId.of("org-sumida"))).thenReturn(Optional.empty());
        givenOrganization(ORG_ID, "Escola Alfa");

        assertThat(sut.execute(USER_ID)).extracting("token").containsExactly("bom");
    }
}
