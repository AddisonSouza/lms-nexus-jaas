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
