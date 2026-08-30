package br.edu.lms.module.organization.domain.model;

/**
 * Dados de exibição de um usuário, obtidos do módulo identity pela
 * {@link br.edu.lms.module.organization.domain.port.out.UserDirectoryPort}.
 */
public record UserProfile(String userId, String fullName, String email) {}
