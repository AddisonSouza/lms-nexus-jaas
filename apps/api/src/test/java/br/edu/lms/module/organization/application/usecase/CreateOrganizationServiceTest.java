package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.CreateOrganizationCommand;
import br.edu.lms.module.organization.domain.event.OrganizationCreatedEvent;
import br.edu.lms.module.organization.domain.exception.OrganizationNameAlreadyExistsException;
import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.model.Organization;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import jakarta.enterprise.event.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrganizationServiceTest {

    @Mock OrganizationRepository organizationRepository;
    @Mock OrganizationMemberRepository organizationMemberRepository;
    @Mock Event<OrganizationCreatedEvent> organizationCreatedEvent;

    @InjectMocks CreateOrganizationService sut;

    private CreateOrganizationCommand cmd(String name, String ownerId) {
        return CreateOrganizationCommand.builder().name(name).description(null).ownerId(ownerId).build();
    }

    @Test
    void shouldCreateOrganizationAndLinkOwnerAsAdminOrg() {
        var ownerId = "user-1";
        when(organizationRepository.findByOwnerIdAndName(ownerId, "My School")).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(organizationMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(cmd("My School", ownerId));

        assertThat(result.getName()).isEqualTo("My School");
        assertThat(result.getOwnerId()).isEqualTo(ownerId);

        var memberCaptor = ArgumentCaptor.forClass(OrganizationMember.class);
        verify(organizationMemberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRole()).isEqualTo(MemberRole.ADMIN_ORG);
        assertThat(memberCaptor.getValue().getUserId()).isEqualTo(ownerId);
    }

    @Test
    void shouldPublishOrganizationCreatedEvent() {
        when(organizationRepository.findByOwnerIdAndName(any(), any())).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(organizationMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(cmd("Escola X", "user-2"));

        verify(organizationCreatedEvent).fire(any(OrganizationCreatedEvent.class));
    }

    @Test
    void shouldThrowWhenNameAlreadyExistsForOwner() {
        var existing = Organization.builder()
                .id(OrganizationId.generate()).name("Dup").ownerId("user-1").build();
        when(organizationRepository.findByOwnerIdAndName("user-1", "Dup")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(cmd("Dup", "user-1")))
                .isInstanceOf(OrganizationNameAlreadyExistsException.class);

        verify(organizationRepository, never()).save(any());
    }

    @Test
    void shouldAllowSameNameForDifferentOwners() {
        when(organizationRepository.findByOwnerIdAndName("user-A", "School")).thenReturn(Optional.empty());
        when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(organizationMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> sut.execute(cmd("School", "user-A"))).doesNotThrowAnyException();
    }
}
