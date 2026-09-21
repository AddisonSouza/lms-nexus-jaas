import { useQuery } from '@tanstack/react-query'
import { listAnnouncements } from '../api/announcements'
import { announcementKeys } from '../api/query-keys'

/**
 * `isMember` desliga a busca para quem não pertence à turma: o endpoint exige
 * associação e responderia 403, e o mural nem é renderizado nesse caso.
 */
export function useAnnouncements(classroomId: string, isMember = true) {
  return useQuery({
    queryKey: announcementKeys.byClassroom(classroomId),
    queryFn: () => listAnnouncements(classroomId),
    enabled: !!classroomId && isMember,
  })
}
