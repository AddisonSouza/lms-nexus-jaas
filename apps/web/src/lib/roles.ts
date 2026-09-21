// Hierarquia ADMIN_ORG > GESTOR > PROFESSOR: quem está acima também pode lecionar.
// O que cada um acessa continua limitado pelo vínculo com disciplina, turma ou tarefa.
const TEACHING_ROLES = ['PROFESSOR', 'GESTOR', 'ADMIN_ORG']

export function canTeach(role: string | null): boolean {
  return role !== null && TEACHING_ROLES.includes(role)
}

const roleLabels: Record<string, string> = {
  ADMIN_ORG: 'Administrador',
  GESTOR: 'Gestor',
  PROFESSOR: 'Professor',
  ALUNO: 'Aluno',
}

/**
 * Rótulo legível do papel de um membro. Vive fora das features porque vários
 * painéis exibem papéis e features não podem importar umas às outras. Papel
 * desconhecido volta como veio.
 */
export function roleLabel(role: string): string {
  return roleLabels[role] ?? role
}

/**
 * Gates por ação, espelhando o que o backend já recusa. Ficam juntos aqui porque
 * as comparações inline espalhadas pelas telas divergiam da regra do servidor e
 * ofereciam botões que só davam 403.
 */

/** `SubjectResource.delete` é `@RolesAllowed(ADMIN_ORG)` — gestor não exclui. */
export function canDeleteSubject(role: string | null): boolean {
  return role === 'ADMIN_ORG'
}

/** Criar e editar disciplina: administrador e gestor. Excluir é mais estrito. */
export function canManageSubject(role: string | null): boolean {
  return role === 'ADMIN_ORG' || role === 'GESTOR'
}

/**
 * Entrar em turma por código é ação de aluno. O backend ainda aceita qualquer
 * papel (fora do escopo deste card); o gate aqui é o que a tela oferece.
 */
export function canJoinByCode(role: string | null): boolean {
  return role === 'ALUNO'
}
