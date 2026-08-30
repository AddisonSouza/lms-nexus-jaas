import type { MemberRole, AssignableRole } from './api/organization-api'

export const roleLabels: Record<MemberRole, string> = {
  ADMIN_ORG: 'Administrador',
  GESTOR: 'Gestor',
  PROFESSOR: 'Professor',
  ALUNO: 'Aluno',
}

/** Papéis que um ADMIN_ORG pode atribuir — ADMIN_ORG é de quem criou a organização. */
export const assignableRoles = ['GESTOR', 'PROFESSOR', 'ALUNO'] as const

/**
 * O papel do membro é editável na tela? ADMIN_ORG não está entre os atribuíveis,
 * então um membro com esse papel é exibido sem o seletor, e não com uma opção
 * inexistente que mostraria o papel errado.
 */
export function isAssignableRole(role: MemberRole): role is AssignableRole {
  return (assignableRoles as readonly string[]).includes(role)
}
