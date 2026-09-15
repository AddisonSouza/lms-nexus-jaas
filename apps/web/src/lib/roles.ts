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
