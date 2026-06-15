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
