export const classroomKeys = {
  all: ['classrooms'] as const,
  lists: () => [...classroomKeys.all, 'list'] as const,
  detail: (id: string) => [...classroomKeys.all, 'detail', id] as const,
  members: (id: string) => [...classroomKeys.all, 'members', id] as const,
}

/**
 * Busca no diretório da organização — cache separado do da turma, porque o
 * termo buscado faz parte da chave e nada disso pertence a uma turma.
 */
export const orgDirectoryKeys = {
  all: ['org-directory'] as const,
  search: (organizationId: string, term: string) =>
    [...orgDirectoryKeys.all, organizationId, 'search', term] as const,
}
