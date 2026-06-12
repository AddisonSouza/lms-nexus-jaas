package br.edu.lms.module.organization.domain.event;

public record MemberInvitedEvent(String organizationId, String email, String token) {
}
