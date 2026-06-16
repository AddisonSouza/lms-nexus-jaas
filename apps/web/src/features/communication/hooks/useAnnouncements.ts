import { useQuery } from '@tanstack/react-query'
import { listAnnouncements } from '../api/announcements'
import { announcementKeys } from '../api/query-keys'

export function useAnnouncements(classroomId: string) {
  return useQuery({
    queryKey: announcementKeys.byClassroom(classroomId),
    queryFn: () => listAnnouncements(classroomId),
    enabled: !!classroomId,
  })
}
