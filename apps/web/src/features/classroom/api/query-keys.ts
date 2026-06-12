export const classroomKeys = {
  all: ['classrooms'] as const,
  lists: () => [...classroomKeys.all, 'list'] as const,
  detail: (id: string) => [...classroomKeys.all, 'detail', id] as const,
  members: (id: string) => [...classroomKeys.all, 'members', id] as const,
}
