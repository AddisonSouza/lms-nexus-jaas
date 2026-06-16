export const taskKeys = {
  all: ['tasks'] as const,
  lists: () => [...taskKeys.all, 'list'] as const,
  published: () => [...taskKeys.all, 'published'] as const,
  detail: (id: string) => [...taskKeys.all, 'detail', id] as const,
}

export const submissionKeys = {
  all: ['submissions'] as const,
  byTask: (taskId: string) => [...submissionKeys.all, 'task', taskId] as const,
}
