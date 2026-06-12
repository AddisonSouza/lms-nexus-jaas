package br.edu.lms.module.organization.domain.event;

import br.edu.lms.module.organization.domain.model.OrganizationId;

public record OrganizationCreatedEvent(OrganizationId organizationId, String ownerId) {
}
