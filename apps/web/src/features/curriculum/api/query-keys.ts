export const subjectKeys = {
  all: ['subjects'] as const,
  lists: () => [...subjectKeys.all, 'list'] as const,
  detail: (id: string) => [...subjectKeys.all, 'detail', id] as const,
}
