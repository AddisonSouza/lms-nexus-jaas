export const announcementKeys = {
  all: ['announcements'] as const,
  byClassroom: (classroomId: string) => [...announcementKeys.all, 'classroom', classroomId] as const,
}

export const notificationKeys = {
  all: ['notifications'] as const,
}
