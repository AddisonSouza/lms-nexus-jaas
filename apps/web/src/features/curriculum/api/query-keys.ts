export const subjectKeys = {
  all: ['subjects'] as const,
  lists: () => [...subjectKeys.all, 'list'] as const,
  detail: (id: string) => [...subjectKeys.all, 'detail', id] as const,
}

export const topicKeys = {
  all: ['topics'] as const,
  bySubject: (subjectId: string) => [...topicKeys.all, 'subject', subjectId] as const,
}

export const contentKeys = {
  all: ['contents'] as const,
  bySubject: (subjectId: string) => [...contentKeys.all, 'subject', subjectId] as const,
}

/** Listas da organização consultadas pela disciplina (turmas e membros). */
export const orgDirectoryKeys = {
  all: ['curriculum', 'org-directory'] as const,
  classrooms: () => [...orgDirectoryKeys.all, 'classrooms'] as const,
  members: (organizationId: string) =>
    [...orgDirectoryKeys.all, 'members', organizationId] as const,
}
