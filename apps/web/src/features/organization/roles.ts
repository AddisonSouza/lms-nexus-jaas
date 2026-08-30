import type { MemberRole } from './api/organization-api'

export const roleLabels: Record<MemberRole, string> = {
  ADMIN_ORG: 'Administrador',
  GESTOR: 'Gestor',
  PROFESSOR: 'Professor',
  ALUNO: 'Aluno',
}

/** Papéis que um ADMIN_ORG pode atribuir — ADMIN_ORG é de quem criou a organização. */
export const assignableRoles = ['GESTOR', 'PROFESSOR', 'ALUNO'] as const
