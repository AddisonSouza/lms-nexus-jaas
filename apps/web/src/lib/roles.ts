// Hierarquia ADMIN_ORG > GESTOR > PROFESSOR: quem está acima também pode lecionar.
// O que cada um acessa continua limitado pelo vínculo com disciplina, turma ou tarefa.
const TEACHING_ROLES = ['PROFESSOR', 'GESTOR', 'ADMIN_ORG']

export function canTeach(role: string | null): boolean {
  return role !== null && TEACHING_ROLES.includes(role)
}
