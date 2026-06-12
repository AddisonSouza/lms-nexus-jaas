package br.edu.lms.module.organization.application.usecase;

import br.edu.lms.module.organization.application.dto.CreateOrganizationCommand;
import br.edu.lms.module.organization.application.dto.OrganizationResponse;
import br.edu.lms.module.organization.domain.event.OrganizationCreatedEvent;
import br.edu.lms.module.organization.domain.exception.OrganizationNameAlreadyExistsException;
import br.edu.lms.module.organization.domain.model.MemberRole;
import br.edu.lms.module.organization.domain.model.Organization;
import br.edu.lms.module.organization.domain.model.OrganizationId;
import br.edu.lms.module.organization.domain.model.OrganizationMember;
import br.edu.lms.module.organization.domain.port.in.CreateOrganizationUseCase;
import br.edu.lms.module.organization.domain.port.out.OrganizationMemberRepository;
import br.edu.lms.module.organization.domain.port.out.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class CreateOrganizationService implements CreateOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final Event<OrganizationCreatedEvent> organizationCreatedEvent;

    @Override
    public OrganizationResponse execute(CreateOrganizationCommand command) {
        organizationRepository.findByOwnerIdAndName(command.getOwnerId(), command.getName())
                .ifPresent(existing -> { throw new OrganizationNameAlreadyExistsException(); });

        var org = Organization.builder()
                .id(OrganizationId.generate())
                .name(command.getName())
                .description(command.getDescription())
                .ownerId(command.getOwnerId())
                .build();

        var saved = organizationRepository.save(org);

        var member = OrganizationMember.builder()
                .id(UUID.randomUUID().toString())
                .organizationId(saved.getId().getValue())
                .userId(command.getOwnerId())
                .role(MemberRole.ADMIN_ORG)
                .build();

        organizationMemberRepository.save(member);
        organizationCreatedEvent.fire(new OrganizationCreatedEvent(saved.getId(), command.getOwnerId()));

        log.info("Organization created: {} by user: {}", saved.getId().getValue(), command.getOwnerId());

        return OrganizationResponse.builder()
                .id(saved.getId().getValue())
                .name(saved.getName())
                .description(saved.getDescription())
                .ownerId(saved.getOwnerId())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
