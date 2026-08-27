package br.edu.lms.module.identity.domain.model;

/**
 * The user behind a refresh token and the organization the session is in.
 * {@code organizationId} is null while the user has no organization.
 */
public record RefreshSession(String userId, String organizationId) {
}
